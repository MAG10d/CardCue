package com.cardcue.app.model

import androidx.compose.ui.graphics.Color

data class CreditCardBill(
    val bankName: String,
    val cardNumber: String,
    val totalDue: String,
    val minDue: String,
    val dueDate: String,
    val dueDateIso: String,
    val daysLeft: Int,
    val cardColor: List<Color>,
    val status: BillStatus,
    val logoResId: Int
)
