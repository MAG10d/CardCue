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
import com.cardcue.app.model.CardUiState
import com.cardcue.app.ui.AddCardScreen
import com.cardcue.app.ui.BillDetailScreen
import com.cardcue.app.ui.CalendarScreen
import com.cardcue.app.ui.EditBillScreen
import com.cardcue.app.ui.HomeScreen
import com.cardcue.app.ui.HomeViewModel
import com.cardcue.app.ui.HomeViewModelFactory
import com.cardcue.app.ui.SetBillDialog
import com.cardcue.app.ui.SettingsScreen
import com.cardcue.app.ui.navigation.Screen
import com.cardcue.app.ui.theme.CardCueTheme

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
            val useDynamicColors = viewModel.useDynamicColors.collectAsState(initial = true).value

            CardCueTheme(
                darkTheme = isDarkMode,
                dynamicColor = useDynamicColors
            ) {
                val navController = rememberNavController()

                // State for "Set Bill" Dialog
                var showSetBillDialog by remember { mutableStateOf(false) }
                var selectedCardState by remember { mutableStateOf<CardUiState?>(null) }

                if (showSetBillDialog && selectedCardState != null) {
                    // Determine if we are updating an existing bill or adding a new one
                    val existingBill = selectedCardState!!.latestBill

                    SetBillDialog(
                        initialAmount = existingBill?.amount,
                        initialDate = existingBill?.dueDate,
                        onDismiss = { showSetBillDialog = false },
                        onSave = { amount, dueDate ->
                            if (existingBill != null) {
                                // Update existing bill
                                viewModel.updateBill(existingBill.copy(amount = amount, dueDate = dueDate))
                            } else {
                                // Add new bill
                                viewModel.addBillToCard(selectedCardState!!.card.id, amount, dueDate)
                            }
                            showSetBillDialog = false
                        }
                    )
                }

                // Navigation Host
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    // Fancy Animations
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
                    popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
                    popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
                ) {
                    
                    // --- HOME SCREEN ---
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onBottomNavClick = { route ->
                                if (route != Screen.Home.route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            onAddCardClick = { navController.navigate(Screen.AddBill.route) }, // Reusing AddBill route for AddCard for now
                            onCardClick = { cardState ->
                                selectedCardState = cardState
                                showSetBillDialog = true
                            }
                        )
                    }

                    // --- ADD CARD SCREEN (Replaces AddBill) ---
                    composable(Screen.AddBill.route) {
                        AddCardScreen(
                            onBackClick = { navController.popBackStack() },
                            onSaveClick = { card, bill ->
                                viewModel.addCard(card, bill)
                                navController.popBackStack()
                            }
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
