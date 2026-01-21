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
import com.cardcue.app.data.BillEntity
import com.cardcue.app.data.BillRepository
import com.cardcue.app.model.BillStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
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

fun BillEntity.toUiState(): BillEntryUiState = BillEntryUiState(
    id = id,
    bankName = bankName,
    cardNumber = cardNumber,
    totalDue = amount.toString(),
    minDue = (amount / 10).toString(), // Mock logic as Entity lacks minDue
    dueDate = dueDate,
    recurringDayOfMonth = null, // Entity lacks this
    reminderOffsets = emptyList(), // Entity lacks this
    status = if (isPaid) BillStatus.PAID else BillStatus.UNPAID,
    cardColor = colorArgb,
    isEntryValid = true
)

fun BillEntryUiState.toBill(): BillEntity = BillEntity(
    id = id,
    bankName = bankName,
    cardNumber = cardNumber,
    amount = totalDue.toDoubleOrNull() ?: 0.0,
    dueDate = dueDate ?: System.currentTimeMillis(),
    isPaid = status == BillStatus.PAID,
    colorArgb = cardColor
)

class BillEntryViewModel(private val repository: BillRepository) : ViewModel() {

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
                    repository.insert(bill)
                } else {
                    repository.update(bill)
                }
            }
        }
    }

    fun loadBill(billId: Int) {
        viewModelScope.launch {
            val bill = repository.getBillById(billId)
            if (bill != null) {
                _uiState.value = bill.toUiState()
            }
        }
    }

    fun toggleBillStatus() {
        val currentStatus = _uiState.value.status
        val newStatus = if (currentStatus == BillStatus.PAID) BillStatus.UNPAID else BillStatus.PAID

        // Update local state immediately for UI responsiveness
        val updatedState = _uiState.value.copy(status = newStatus)
        _uiState.value = updatedState

        // Persist to DB
        viewModelScope.launch {
            repository.update(updatedState.toBill())
        }
    }

    fun deleteBill() {
        val bill = _uiState.value.toBill()
        if (bill.id != 0) {
            viewModelScope.launch {
                repository.delete(bill)
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
                BillEntryViewModel(application.billRepository)
            }
        }
    }
}
