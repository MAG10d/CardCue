package com.cardcue.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cardcue.app.ui.util.FormattingUtils
import com.cardcue.app.ui.viewmodel.BillEntryUiState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillEntryBody(
    billUiState: BillEntryUiState,
    onBillValueChange: (BillEntryUiState) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BillInputForm(
            billUiState = billUiState,
            onValueChange = onBillValueChange
        )
        Button(
            onClick = onSaveClick,
            enabled = billUiState.isEntryValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillInputForm(
    billUiState: BillEntryUiState,
    onValueChange: (BillEntryUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = billUiState.bankName,
            onValueChange = { onValueChange(billUiState.copy(bankName = it)) },
            label = { Text("Bank Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = billUiState.cardNumber,
            onValueChange = { onValueChange(billUiState.copy(cardNumber = it)) },
            label = { Text("Card Number (Last 4)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        OutlinedTextField(
            value = billUiState.totalDue,
            onValueChange = { onValueChange(billUiState.copy(totalDue = it)) },
            label = { Text("Total Due") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            prefix = { Text("₹") }
        )

        // Due Date Picker
        OutlinedTextField(
            value = billUiState.dueDate?.let { FormattingUtils.formatDate(it) } ?: "",
            onValueChange = {},
            label = { Text("Due Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            }
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = billUiState.dueDate ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onValueChange(billUiState.copy(dueDate = it))
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Recurring Date Toggle
        var isRecurring by remember { mutableStateOf(billUiState.recurringDayOfMonth != null) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = isRecurring,
                    onValueChange = {
                        isRecurring = it
                        if (!it) onValueChange(billUiState.copy(recurringDayOfMonth = null))
                        else onValueChange(billUiState.copy(recurringDayOfMonth = 5)) // Default to 5th
                    },
                    role = Role.Switch
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Recurring Bill", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = isRecurring, onCheckedChange = null)
        }

        if (isRecurring) {
            // Simple Number Input for Day of Month for now (1-31)
             OutlinedTextField(
                value = billUiState.recurringDayOfMonth?.toString() ?: "",
                onValueChange = {
                    val day = it.toIntOrNull()
                    if (day != null && day in 1..31) {
                         onValueChange(billUiState.copy(recurringDayOfMonth = day))
                    }
                },
                label = { Text("Day of Month (1-31)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        // Reminders
        Text(text = "Reminders", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val options = mapOf(0 to "On Due Day", 1 to "1 Day Before", 3 to "3 Days Before", 7 to "1 Week Before")

            options.forEach { (days, label) ->
                val selected = billUiState.reminderOffsets.contains(days) || (days == 0 && billUiState.reminderOffsets.contains(0))
                FilterChip(
                    selected = selected,
                    onClick = {
                        val current = billUiState.reminderOffsets.toMutableList()
                        if (current.contains(days)) {
                            current.remove(days)
                        } else {
                            current.add(days)
                        }
                        onValueChange(billUiState.copy(reminderOffsets = current))
                    },
                    label = { Text(label) }
                )
            }
        }
    }
}
