package com.cardcue.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cardcue.app.model.BillStatus
import com.cardcue.app.model.CreditCardBill
import com.cardcue.app.ui.AddBillScreen
import com.cardcue.app.ui.CalendarScreen
import com.cardcue.app.ui.CreditCardItem
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

// Helpers for Icons
object BankIcons {
    const val HDFC = 1
    const val INDUSIND = 2
    const val ICICI = 3
    const val OTHER = 4
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CardCueTheme {
                val navController = rememberNavController()

                // Mock Data for July 2025
                val bills = listOf(
                    CreditCardBill(
                        bankName = "HDFC Bank",
                        cardNumber = "4582",
                        totalDue = "₹12,450.00",
                        minDue = "₹850.00",
                        dueDate = "03 Jul",
                        dueDateIso = "2025-07-03",
                        daysLeft = 0, // Logic for days left not dynamic for mock
                        cardColor = listOf(RedGradientStart, RedGradientEnd),
                        status = BillStatus.PAID,
                        logoResId = BankIcons.HDFC
                    ),
                    CreditCardBill(
                        bankName = "IndusInd Bank",
                        cardNumber = "9012",
                        totalDue = "₹45,200.50",
                        minDue = "₹2,500.00",
                        dueDate = "05 Jul",
                        dueDateIso = "2025-07-05",
                        daysLeft = 2,
                        cardColor = listOf(PurpleGradientStart, PurpleGradientEnd),
                        status = BillStatus.PAID,
                        logoResId = BankIcons.INDUSIND
                    ),
                    CreditCardBill(
                        bankName = "ICICI Bank",
                        cardNumber = "3341",
                        totalDue = "₹5,600.00",
                        minDue = "₹0.00",
                        dueDate = "30 Jul",
                        dueDateIso = "2025-07-30",
                        daysLeft = 27,
                        cardColor = listOf(Color(0xFF11998e), Color(0xFF38ef7d)),
                        status = BillStatus.DUE,
                        logoResId = BankIcons.ICICI
                    )
                )

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
                    composable(Screen.AddBill.route) {
                        AddBillScreen(
                            onBackClick = { navController.popBackStack() },
                            onSaveClick = { bankName, _, _, _ ->
                                println("Bill Saved: $bankName")
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(Screen.Calendar.route) {
                        CalendarScreen(
                            bills = bills,
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
                        // Placeholder for Settings
                        Scaffold(
                            bottomBar = {
                                BottomNavBar(
                                    selectedItem = Screen.Settings.route,
                                    onItemSelected = { route ->
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Text("Settings Screen", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
            }
        }
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
