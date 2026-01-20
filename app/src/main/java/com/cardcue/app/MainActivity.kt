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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardcue.app.model.CreditCardBill
import com.cardcue.app.ui.CreditCardItem
import com.cardcue.app.ui.components.BottomNavBar
import com.cardcue.app.ui.components.StatBox
import com.cardcue.app.ui.theme.CardCueTheme
import com.cardcue.app.ui.theme.PurpleGradientEnd
import com.cardcue.app.ui.theme.PurpleGradientStart
import com.cardcue.app.ui.theme.RedGradientEnd
import com.cardcue.app.ui.theme.RedGradientStart
import com.cardcue.app.ui.theme.StatTextLate
import com.cardcue.app.ui.theme.StatTextPaid

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CardCueTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = { BottomNavBar() }
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
                text = "Showing all 4 statements",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(count = "4", label = "Total", isSelected = true)
                StatBox(count = "3", label = "Due")
                StatBox(count = "0", label = "Late", textColor = StatTextLate)
                StatBox(count = "1", label = "Paid", textColor = StatTextPaid)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Credit Card List
            CreditCardList()
        }
    }
}

@Composable
fun CreditCardList() {
    // Mock Data
    val bills = listOf(
        CreditCardBill(
            bankName = "HDFC Bank",
            cardNumber = "4582",
            totalDue = "₹12,450.00",
            minDue = "₹850.00",
            dueDate = "24 Aug",
            daysLeft = 5,
            cardColor = listOf(RedGradientStart, RedGradientEnd)
        ),
        CreditCardBill(
            bankName = "Axis Bank",
            cardNumber = "9012",
            totalDue = "₹45,200.50",
            minDue = "₹2,500.00",
            dueDate = "01 Sep",
            daysLeft = 12,
            cardColor = listOf(PurpleGradientStart, PurpleGradientEnd)
        ),
        CreditCardBill(
            bankName = "ICICI Bank",
            cardNumber = "3341",
            totalDue = "₹5,600.00",
            minDue = "₹0.00",
            dueDate = "10 Sep",
            daysLeft = 21,
            cardColor = listOf(Color(0xFF11998e), Color(0xFF38ef7d)) // Green Gradient
        )
    )

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
