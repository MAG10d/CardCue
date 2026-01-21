package com.cardcue.app.model

import com.cardcue.app.data.BillEntity
import com.cardcue.app.data.CardEntity

data class CardUiState(
    val card: CardEntity,
    val latestBill: BillEntity? = null,
    val isBillForCurrentMonth: Boolean = false
)
