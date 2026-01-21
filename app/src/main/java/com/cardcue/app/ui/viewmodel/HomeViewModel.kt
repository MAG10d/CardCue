package com.cardcue.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cardcue.app.CardCueApplication
import com.cardcue.app.data.BillStatus
import com.cardcue.app.data.BillsRepository
import com.cardcue.app.data.CreditCardBill
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val bills: List<CreditCardBill> = emptyList(),
    val totalUnpaid: Double = 0.0,
    val dueCount: Int = 0,
    val lateCount: Int = 0
)

class HomeViewModel(private val repository: BillsRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.getAllBillsStream()
        .map { bills ->
            val now = System.currentTimeMillis()
            val total = bills.filter { it.status == BillStatus.UNPAID }.sumOf { it.totalDue }

            val late = bills.count {
                it.status == BillStatus.UNPAID && it.dueDate < now
            }

            // Due within 7 days
            val due = bills.count {
                if (it.status == BillStatus.PAID) return@count false
                val diff = it.dueDate - now
                // Future and within 7 days (7 * 24 * 60 * 60 * 1000)
                diff > 0 && diff <= TimeUnit.DAYS.toMillis(7)
            }

            HomeUiState(
                bills = bills,
                totalUnpaid = total,
                dueCount = due,
                lateCount = late
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CardCueApplication)
                HomeViewModel(application.container.billsRepository)
            }
        }
    }
}
