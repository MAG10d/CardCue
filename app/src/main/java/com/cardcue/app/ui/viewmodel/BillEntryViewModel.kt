package com.cardcue.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class BillEntryUiState(
    val id: Int = 0,
    val bankName: String = "",
    val cardNumber: String = "",
    val totalDue: String = "",
    val minDue: String = "",
    val dueDate: Long? = null,
    val recurringDayOfMonth: Int? = null,
    val reminderOffsets: List<Int> = emptyList(),
    val status: BillStatus = BillStatus.UNPAID,
    val cardColor: Int = 0xFF000000.toInt(),
    val isEntryValid: Boolean = false
)

fun CreditCardBill.toUiState(): BillEntryUiState = BillEntryUiState(
    id = id,
    bankName = bankName,
    cardNumber = cardNumber,
    totalDue = totalDue.toString(),
    minDue = minDue.toString(),
    dueDate = dueDate,
    recurringDayOfMonth = recurringDayOfMonth,
    reminderOffsets = reminderOffsets,
    status = status,
    cardColor = cardColor,
    isEntryValid = true // Assumed valid if existing
)

fun BillEntryUiState.toBill(): CreditCardBill = CreditCardBill(
    id = id,
    bankName = bankName,
    cardNumber = cardNumber,
    totalDue = totalDue.toDoubleOrNull() ?: 0.0,
    minDue = minDue.toDoubleOrNull() ?: 0.0,
    dueDate = dueDate ?: System.currentTimeMillis(),
    recurringDayOfMonth = recurringDayOfMonth,
    reminderOffsets = reminderOffsets,
    status = status,
    cardColor = cardColor
)

class BillEntryViewModel(private val repository: BillsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BillEntryUiState())
    val uiState: StateFlow<BillEntryUiState> = _uiState.asStateFlow()

    fun updateUiState(newState: BillEntryUiState) {
        _uiState.update {
            newState.copy(isEntryValid = validateInput(newState))
        }
    }

    private fun validateInput(uiState: BillEntryUiState = _uiState.value): Boolean {
        return with(uiState) {
            bankName.isNotBlank() &&
            cardNumber.isNotBlank() &&
            totalDue.toDoubleOrNull() != null &&
            dueDate != null
        }
    }

    fun saveBill() {
        if (validateInput()) {
            viewModelScope.launch {
                val bill = _uiState.value.toBill()
                if (bill.id == 0) {
                    repository.insertBill(bill)
                } else {
                    repository.updateBill(bill)
                }
            }
        }
    }

    fun loadBill(billId: Int) {
        viewModelScope.launch {
            repository.getBillStream(billId)?.let { bill ->
                _uiState.value = bill.toUiState()
            }
        }
    }

    fun deleteBill() {
        val bill = _uiState.value.toBill()
        if (bill.id != 0) {
            viewModelScope.launch {
                repository.deleteBill(bill)
            }
        }
    }

    fun reset() {
        _uiState.value = BillEntryUiState()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CardCueApplication)
                BillEntryViewModel(application.container.billsRepository)
            }
        }
    }
}
