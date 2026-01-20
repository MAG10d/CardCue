package com.cardcue.app.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cardcue.app.data.BillEntity
import com.cardcue.app.data.BillRepository
import com.cardcue.app.data.UserPreferencesRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class HomeViewModel(
    private val billRepository: BillRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val allBills: StateFlow<List<BillEntity>> = billRepository.allBills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    fun addBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.insert(bill)
        }
    }

    fun updateBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.update(bill)
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.delete(bill)
        }
    }

    fun togglePaidStatus(bill: BillEntity) {
        val updatedBill = bill.copy(isPaid = !bill.isPaid)
        updateBill(updatedBill)
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBiometricEnabled(enabled)
        }
    }

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bills = allBills.value // Get current snapshot
                val json = Gson().toJson(bills)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write(json)
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export Successful", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readText()
                    }
                }

                if (json != null) {
                    val type = object : TypeToken<List<BillEntity>>() {}.type
                    val bills: List<BillEntity> = Gson().fromJson(json, type)

                    // Insert all imported bills
                    bills.forEach { bill ->
                        // Reset ID to 0 to ensure auto-generation/avoid conflicts
                        billRepository.insert(bill.copy(id = 0))
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Import Successful: ${bills.size} bills added", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Import Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
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
