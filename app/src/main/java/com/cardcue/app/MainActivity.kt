package com.cardcue.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*
import com.cardcue.app.data.BillEntity
import com.cardcue.app.model.BillStatus
import com.cardcue.app.model.CreditCardBill
import com.cardcue.app.ui.AddBillScreen
import com.cardcue.app.ui.BillDetailScreen
import com.cardcue.app.ui.CalendarScreen
import com.cardcue.app.ui.EditBillScreen
import com.cardcue.app.ui.HomeScreen
import com.cardcue.app.ui.HomeViewModel
import com.cardcue.app.ui.HomeViewModelFactory
import com.cardcue.app.ui.SettingsScreen
import com.cardcue.app.ui.navigation.Screen
import com.cardcue.app.ui.theme.CardCueTheme
import com.cardcue.app.ui.theme.PurpleGradientEnd
import com.cardcue.app.ui.theme.PurpleGradientStart
import com.cardcue.app.ui.theme.RedGradientEnd
import com.cardcue.app.ui.theme.RedGradientStart
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Helpers for Icons
object BankIcons {
    const val HDFC = 1
    const val INDUSIND = 2
    const val ICICI = 3
    const val OTHER = 4
}

class MainActivity : FragmentActivity() {

    private lateinit var viewModel: HomeViewModel
    private var isLocked = false
    private var lastBackgroundTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel with Repository from Application
        val application = application as CardCueApplication
        val factory = HomeViewModelFactory(application.billRepository, application.userPreferencesRepository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Lifecycle observer for Biometric Lock
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                lastBackgroundTime = System.currentTimeMillis()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                // Check if should lock (after 1 minute in background)
                if (System.currentTimeMillis() - lastBackgroundTime > 60000) {
                    // Check settings asynchronously
                    val isBioEnabled = viewModel.isBiometricEnabled.value
                    if (isBioEnabled && !isLocked) {
                        isLocked = true
                        showBiometricPrompt()
                    }
                }
            }
        })

        setContent {
            val isDarkMode = viewModel.isDarkMode.collectAsState(initial = false).value

            CardCueTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                // 1. Observe Real Data from Database
                val billsEntities = viewModel.allBills.collectAsState(initial = emptyList()).value

                // 2. Map Database Entity to UI Model
                val bills = billsEntities.map { entity ->
                    CreditCardBill(
                        bankName = entity.bankName,
                        cardNumber = entity.cardNumber,
                        totalDue = "₹${entity.amount}",
                        minDue = "₹${entity.amount / 10}",
                        dueDate = Instant.ofEpochMilli(entity.dueDate).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM")),
                        dueDateIso = Instant.ofEpochMilli(entity.dueDate).atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                        daysLeft = 0, // You can add logic to calculate days left here
                        cardColor = if (entity.bankName.contains("HDFC", true)) listOf(RedGradientStart, RedGradientEnd) else listOf(PurpleGradientStart, PurpleGradientEnd),
                        status = if (entity.isPaid) BillStatus.PAID else BillStatus.DUE,
                        logoResId = 1 // You can implement icon mapping logic here
                    )
                }

                // 3. Navigation Host
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    // Fancy Animations from HEAD branch
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
                    popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
                    popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
                ) {
                    
                    // --- HOME SCREEN ---
                    composable(Screen.Home.route) {
                        HomeScreen(
                            bills = bills, // Pass the mapped bills directly
                            onBottomNavClick = { route ->
                                if (route != Screen.Home.route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            onAddBillClick = { navController.navigate(Screen.AddBill.route) },
                            onBillClick = { billId -> navController.navigate(Screen.BillDetail.createRoute(billId)) }
                        )
                    }

                    // --- ADD BILL SCREEN ---
                    composable(Screen.AddBill.route) {
                        AddBillScreen(
                            onBackClick = { navController.popBackStack() }
                            // onSaveClick removed, handled by ViewModel in AddBillScreen
                        )
                    }

                    // --- DETAIL SCREEN ---
                    composable(
                        route = Screen.BillDetail.route,
                        arguments = Screen.BillDetail.arguments
                    ) { backStackEntry ->
                        val billId = backStackEntry.arguments?.getInt("billId") ?: 0
                        BillDetailScreen(
                            billId = billId,
                            onBackClick = { navController.popBackStack() },
                            onEditClick = { id -> navController.navigate(Screen.EditBill.createRoute(id)) }
                        )
                    }

                    // --- EDIT SCREEN ---
                    composable(
                        route = Screen.EditBill.route,
                        arguments = Screen.EditBill.arguments
                    ) { backStackEntry ->
                        val billId = backStackEntry.arguments?.getInt("billId") ?: 0
                        EditBillScreen(
                            billId = billId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // --- CALENDAR SCREEN ---
                    composable(Screen.Calendar.route) {
                        CalendarScreen(
                            viewModel = viewModel,
                            onBottomNavClick = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // --- SETTINGS SCREEN ---
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                if (navController.previousBackStackEntry != null) {
                                    navController.popBackStack()
                                } else {
                                    navController.navigate(Screen.Home.route)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isLocked = false
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Optional: Close app if user cancels
                    // finish()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
