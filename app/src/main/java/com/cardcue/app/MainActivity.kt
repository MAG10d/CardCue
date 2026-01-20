package com.cardcue.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardcue.app.data.BillEntity
import com.cardcue.app.model.BillStatus
import com.cardcue.app.model.CreditCardBill
import com.cardcue.app.ui.AddBillScreen
import com.cardcue.app.ui.CalendarScreen
import com.cardcue.app.ui.CreditCardItem
import com.cardcue.app.ui.HomeViewModel
import com.cardcue.app.ui.HomeViewModelFactory
import com.cardcue.app.ui.SettingsScreen
import com.cardcue.app.ui.components.BottomNavBar
import com.cardcue.app.ui.components.StatBox
import com.cardcue.app.ui.navigation.Screen
import com.cardcue.app.ui.theme.CardCueTheme
import com.cardcue.app.ui.theme.PurpleGradientEnd
import com.cardcue.app.ui.theme.PurpleGradientStart
import com.cardcue.app.ui.theme.RedGradientEnd
import com.cardcue.app.ui.theme.RedGradientStart
import com.cardcue.app.ui.theme.StatTextLate
import com.cardcue.app.ui.theme.StatTextPaid
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

        val application = application as CardCueApplication
        val factory = HomeViewModelFactory(application.billRepository, application.userPreferencesRepository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Lifecycle observer for Biometric Lock
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                lastBackgroundTime = System.currentTimeMillis()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                // Check if should lock
                if (System.currentTimeMillis() - lastBackgroundTime > 60000) { // 1 minute
                    // Check settings asynchronously (safe for MVP)
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

                // Observe Real Data
                val billsEntities = viewModel.allBills.collectAsState(initial = emptyList()).value

                // Map Entity to UI Model (simplified for HomeScreen consumption)
                val bills = billsEntities.map { entity ->
                     CreditCardBill(
                        bankName = entity.bankName,
                        cardNumber = entity.cardNumber,
                        totalDue = "₹${entity.amount}",
                        minDue = "₹${entity.amount / 10}",
                        dueDate = Instant.ofEpochMilli(entity.dueDate).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM")),
                        dueDateIso = Instant.ofEpochMilli(entity.dueDate).atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                        daysLeft = 0,
                        cardColor = if (entity.bankName.contains("HDFC")) listOf(RedGradientStart, RedGradientEnd) else listOf(PurpleGradientStart, PurpleGradientEnd),
                        status = if (entity.isPaid) BillStatus.PAID else BillStatus.DUE,
                        logoResId = 1
                    )
                }

                NavHost(navController = navController, startDestination = Screen.Home.route) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            bills = bills,
                            onBottomNavClick = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onAddBillClick = {
                                navController.navigate(Screen.AddBill.route)
                            }
                        )
                    }
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
                    composable(Screen.AddBill.route) {
                        AddBillScreen(
                            onBackClick = { navController.popBackStack() },
                            onSaveClick = { bankName, cardNumber, totalDue, dueDateStr ->
                                try {
                                    // VERY Basic parsing DD/MM/YYYY
                                    val parts = dueDateStr.split("/")
                                    val day = parts[0].toInt()
                                    val month = parts[1].toInt()
                                    val year = parts[2].toInt()
                                    val date = java.time.LocalDate.of(year, month, day)
                                    val epoch = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                                    viewModel.addBill(
                                        BillEntity(
                                            bankName = bankName,
                                            cardNumber = cardNumber,
                                            amount = totalDue.toDoubleOrNull() ?: 0.0,
                                            dueDate = epoch,
                                            isPaid = false,
                                            colorArgb = 0
                                        )
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                navController.popBackStack()
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
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun HomeScreen(
    bills: List<CreditCardBill>,
    onBottomNavClick: (String) -> Unit,
    onAddBillClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedItem = Screen.Home.route,
                onItemSelected = onBottomNavClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBillClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Bill")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Text(
                text = "CardCue",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sub-header
            Text(
                text = "Your Statements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Showing all ${bills.size} statements",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(count = bills.size.toString(), label = "Total", isSelected = true)
                StatBox(count = bills.count { it.status == BillStatus.DUE }.toString(), label = "Due")
                StatBox(count = bills.count { it.status == BillStatus.OVERDUE }.toString(), label = "Late", textColor = StatTextLate)
                StatBox(count = bills.count { it.status == BillStatus.PAID }.toString(), label = "Paid", textColor = StatTextPaid)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Credit Card List
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(bills) { bill ->
                    CreditCardItem(
                        bill = bill
                    )
                }
            }
        }
    }
}
