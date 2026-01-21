package com.cardcue.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class CreditCardBill(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bankName: String,
    val cardNumber: String,
    val totalDue: Double,
    val minDue: Double,
    val dueDate: Long, // Timestamp
    val recurringDayOfMonth: Int? = null,
    val reminderOffsets: List<Int> = emptyList(),
    val status: BillStatus = BillStatus.UNPAID,
    val cardColor: Int // ARGB Int
)
