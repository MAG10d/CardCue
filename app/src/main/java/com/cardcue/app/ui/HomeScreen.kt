package com.cardcue.app.ui

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
import com.cardcue.app.model.BillStatus
import com.cardcue.app.model.CreditCardBill
import com.cardcue.app.ui.components.BottomNavBar
import com.cardcue.app.ui.components.StatBox
import com.cardcue.app.ui.navigation.Screen
import com.cardcue.app.ui.theme.StatTextLate
import com.cardcue.app.ui.theme.StatTextPaid

@Composable
fun HomeScreen(
    bills: List<CreditCardBill>, // Accept data directly instead of ViewModel
    onBottomNavClick: (String) -> Unit,
    onAddBillClick: () -> Unit,
    onBillClick: (Int) -> Unit
) {
    // Calculate Stats from the list
    val totalCount = bills.size
    val dueCount = bills.count { it.status == BillStatus.DUE }
    val paidCount = bills.count { it.status == BillStatus.PAID }
    // Basic late check logic could go here, for now assuming 0
    val lateCount = 0

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
                text = "Showing all $totalCount statements",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(count = totalCount.toString(), label = "Total", isSelected = true)
                StatBox(count = dueCount.toString(), label = "Due")
                StatBox(count = lateCount.toString(), label = "Late", textColor = StatTextLate)
                StatBox(count = paidCount.toString(), label = "Paid", textColor = StatTextPaid)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Credit Card List
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(bills) { bill ->
                    CreditCardItem(
                        bill = bill,
                        onItemClick = onBillClick
                    )
                }
            }
        }
    }
}
