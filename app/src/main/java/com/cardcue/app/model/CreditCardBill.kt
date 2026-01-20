package com.cardcue.app.model

import androidx.compose.ui.graphics.Color

enum class BillStatus {
    PAID, DUE, OVERDUE
}

data class CreditCardBill(
    val bankName: String,
    val cardNumber: String,
    val totalDue: String,
    val minDue: String,
    val dueDate: String, // Display format string
    val dueDateIso: String, // ISO format YYYY-MM-DD for logic
    val daysLeft: Int,
    val cardColor: List<Color>,
    val status: BillStatus,
    val logoResId: Int
)
