package com.cardcue.app.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cardcue.app.data.BillEntity
import com.cardcue.app.data.BillRepository
import com.cardcue.app.data.CardEntity
import com.cardcue.app.data.UserPreferencesRepository
import com.cardcue.app.model.CardUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class FinancialProfile(
    val salary: Double = 0.0,
    val payday: Int = 1,
    val totalDue: Double = 0.0,
    val remainingBalance: Double = 0.0
)

class HomeViewModel(
    private val billRepository: BillRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Helper for CalendarScreen
    val allBills: StateFlow<List<BillEntity>> = billRepository.allBills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Cards & Latest Bill Logic ---
    val cardUiStates: StateFlow<List<CardUiState>> = billRepository.cardsWithBills
        .combine(billRepository.allBills) { cardsWithBills, _ -> // Trigger update when any bill changes
            cardsWithBills.map { cardWithBills ->
                val card = cardWithBills.card
                val bills = cardWithBills.bills

                // Strategy to find the "Active" bill to show on card
                // 1. Unpaid bills (Priority: Oldest unpaid first? Or just any unpaid?)
                // Requirement: "If there is an UNPAID bill (regardless of month), show that bill."
                // I will pick the one with earliest due date to encourage paying off debt.
                val unpaidBill = bills.filter { !it.isPaid }.minByOrNull { it.dueDate }

                val billToShow = if (unpaidBill != null) {
                    unpaidBill
                } else {
                    // 2. If no unpaid, check for PAID bill in current month
                    // "If the card has a bill for the current month..."
                    // "Current Month" = Due Date falls in current calendar month
                    val currentMonthBills = bills.filter { it.isPaid && isDateInCurrentMonth(it.dueDate) }
                    currentMonthBills.maxByOrNull { it.dueDate } // Latest one
                }

                CardUiState(
                    card = card,
                    latestBill = billToShow,
                    isBillForCurrentMonth = billToShow != null && isDateInCurrentMonth(billToShow.dueDate)
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Financial Profile Logic ---
    private val totalDueFlow = billRepository.getUnpaidBills().combine(billRepository.allBills) { unpaidBills, _ ->
        unpaidBills.sumOf { it.amount }
    }

    val financialProfile: StateFlow<FinancialProfile> = combine(
        userPreferencesRepository.monthlySalary,
        userPreferencesRepository.payday,
        totalDueFlow
    ) { salary, payday, totalDue ->
        FinancialProfile(
            salary = salary,
            payday = payday,
            totalDue = totalDue,
            remainingBalance = salary - totalDue
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialProfile()
    )


    // --- Preferences ---
    val isDarkMode: StateFlow<Boolean> = userPreferencesRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val useDynamicColors: StateFlow<Boolean> = userPreferencesRepository.useDynamicColors
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isBiometricEnabled: StateFlow<Boolean> = userPreferencesRepository.isBiometricEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )


    // --- Actions ---

    fun addCard(card: CardEntity, initialBill: BillEntity?) {
        viewModelScope.launch {
            val cardId = billRepository.insertCard(card)
            initialBill?.let {
                billRepository.insertBill(it.copy(cardId = cardId.toInt()))
            }
        }
    }

    fun addBillToCard(cardId: Int, amount: Double, dueDate: Long) {
        viewModelScope.launch {
            val bill = BillEntity(
                cardId = cardId,
                amount = amount,
                dueDate = dueDate,
                isPaid = false
            )
            billRepository.insertBill(bill)
        }
    }

    fun updateBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.updateBill(bill)
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.deleteBill(bill)
        }
    }

    fun setSalary(salary: Double) {
        viewModelScope.launch {
            userPreferencesRepository.setMonthlySalary(salary)
        }
    }

    fun setPayday(day: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setPayday(day)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDynamicColors(enabled)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBiometricEnabled(enabled)
        }
    }

    // --- Helpers ---
    private fun isDateInCurrentMonth(dueDateMillis: Long): Boolean {
        val dueDate = Instant.ofEpochMilli(dueDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val now = LocalDate.now()
        return dueDate.year == now.year && dueDate.month == now.month
    }

    // --- Deprecated / Temporary Disabled Import/Export (needs refactor for new schema) ---
    fun exportData(context: Context, uri: Uri) {
         // TODO: Implement export for Card+Bill schema
         viewModelScope.launch(Dispatchers.Main) {
             Toast.makeText(context, "Export temporarily disabled for architecture upgrade", Toast.LENGTH_SHORT).show()
         }
    }

    fun importData(context: Context, uri: Uri) {
        // TODO: Implement import for Card+Bill schema
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, "Import temporarily disabled for architecture upgrade", Toast.LENGTH_SHORT).show()
        }
    }
}

class HomeViewModelFactory(
    private val billRepository: BillRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(billRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
