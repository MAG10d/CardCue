package com.cardcue.app.data

import androidx.room.Embedded
import androidx.room.Relation

data class CardWithBills(
    @Embedded val card: CardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "cardId"
    )
    val bills: List<BillEntity>
)
