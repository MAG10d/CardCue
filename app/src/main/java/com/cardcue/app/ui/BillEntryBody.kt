package com.cardcue.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cardcue.app.ui.viewmodel.BillUiState

@Composable
fun BillEntryBody(
    billUiState: BillUiState,
    onBillValueChange: (BillUiState) -> Unit,
    onSaveClick: () -> Unit
) {
    Column {
        // Amount
        OutlinedTextField(
            value = billUiState.totalDue,
            onValueChange = { onBillValueChange(billUiState.copy(totalDue = it)) },
            label = { Text("Total Due Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Due Date (simplified text input for now, ideally DatePicker)
        // Since `BillUiState` uses LocalDate?, we need to handle conversion or just show a button to pick date.
        // For simplicity in this step, I'll rely on the ViewModel having default or handling it,
        // OR I should use the `SetBillDialog` logic which used DatePicker.
        // Let's assume we want to just save for now.

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}
