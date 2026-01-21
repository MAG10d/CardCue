package com.cardcue.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId"])]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardId: Int,
    val amount: Double,
    val dueDate: Long, // Epoch milliseconds
    val isPaid: Boolean
)
