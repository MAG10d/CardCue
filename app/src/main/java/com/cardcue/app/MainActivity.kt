package com.cardcue.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cardcue.app.model.CreditCardBill
import com.cardcue.app.ui.CreditCardItem
import com.cardcue.app.ui.theme.CardCueTheme
import com.cardcue.app.ui.theme.RedGradientEnd
import com.cardcue.app.ui.theme.RedGradientStart

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CardCueTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CreditCardList()
                }
            }
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
            bankName = "SBI Card",
            cardNumber = "9012",
            totalDue = "₹45,200.50",
            minDue = "₹2,500.00",
            dueDate = "01 Sep",
            daysLeft = 12,
            cardColor = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)) // Purple Gradient
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
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(bills) { bill ->
            CreditCardItem(bill = bill)
        }
    }
}
