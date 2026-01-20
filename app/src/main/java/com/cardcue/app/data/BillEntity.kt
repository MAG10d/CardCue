package com.cardcue.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bankName: String,
    val cardNumber: String,
    val amount: Double,
    val dueDate: Long, // Epoch milliseconds
    val isPaid: Boolean,
    val colorArgb: Int // For storing gradient start/end or single color representation
)
