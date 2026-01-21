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

// Alias for compatibility if needed, or I can rename the class in UI files
typealias BillUiState = BillEntryUiState

data class BillEntryUiState(
    val id: Int = 0,
    val cardId: Int = 0,
    val totalDue: String = "",
    val dueDate: Long? = null,
    val status: BillStatus = BillStatus.UNPAID,
    // Extra fields needed for UI display which come from CardEntity
    val bankName: String = "",
    val cardNumber: String = "",
    val cardColor: Int = 0xFF000000.toInt(),
    val isEntryValid: Boolean = false
)

// Mapping helpers need to be async or handle missing card info if we want full details.
// However, BillEntity NO LONGER has bankName, etc.
// The ViewModel needs to fetch Card info too.
// This is the tricky part I anticipated.

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
            totalDue.toDoubleOrNull() != null &&
            dueDate != null
        }
    }

    fun saveBill() {
        if (validateInput()) {
            viewModelScope.launch {
                val state = _uiState.value
                val bill = BillEntity(
                    id = state.id,
                    cardId = state.cardId,
                    amount = state.totalDue.toDoubleOrNull() ?: 0.0,
                    dueDate = state.dueDate ?: System.currentTimeMillis(),
                    isPaid = state.status == BillStatus.PAID
                )
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
            val bill = repository.getBillById(billId)
            if (bill != null) {
                // We need card info to populate UI
                val card = repository.getCardById(bill.cardId)
                _uiState.value = BillEntryUiState(
                    id = bill.id,
                    cardId = bill.cardId,
                    totalDue = bill.amount.toString(),
                    dueDate = bill.dueDate,
                    status = if (bill.isPaid) BillStatus.PAID else BillStatus.UNPAID,
                    bankName = card?.bankName ?: "Unknown",
                    cardNumber = card?.last4Digits ?: "",
                    cardColor = card?.colorArgb ?: 0xFF000000.toInt(),
                    isEntryValid = true
                )
            }
        }
    }

    fun toggleBillStatus() {
        val currentStatus = _uiState.value.status
        val newStatus = if (currentStatus == BillStatus.PAID) BillStatus.UNPAID else BillStatus.PAID

        // Update local state
        _uiState.update { it.copy(status = newStatus) }

        // Persist
        viewModelScope.launch {
            val state = _uiState.value
            val bill = BillEntity(
                id = state.id,
                cardId = state.cardId,
                amount = state.totalDue.toDoubleOrNull() ?: 0.0,
                dueDate = state.dueDate ?: System.currentTimeMillis(),
                isPaid = newStatus == BillStatus.PAID
            )
            repository.updateBill(bill)
        }
    }

    fun deleteBill() {
        val state = _uiState.value
        if (state.id != 0) {
            viewModelScope.launch {
                 val bill = BillEntity(
                    id = state.id,
                    cardId = state.cardId,
                    amount = state.totalDue.toDoubleOrNull() ?: 0.0,
                    dueDate = state.dueDate ?: System.currentTimeMillis(),
                    isPaid = state.status == BillStatus.PAID
                )
                repository.deleteBill(bill)
            }
        }
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
