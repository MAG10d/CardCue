package com.cardcue.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cardcue.app.data.BillEntity
import com.cardcue.app.data.CardEntity
import com.cardcue.app.ui.theme.PurpleGradientStart
import com.cardcue.app.ui.theme.RedGradientStart
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    onBackClick: () -> Unit,
    onSaveClick: (CardEntity, BillEntity?) -> Unit
) {
    // Card Details
    var bankName by remember { mutableStateOf("") }
    var last4Digits by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(RedGradientStart.toArgb()) }

    // Optional Bill Details
    var addInitialBill by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var dueDateString by remember { mutableStateOf("") } // Simple text for now, could use DatePicker

    // Colors Palette
    val colors = listOf(
        RedGradientStart,
        PurpleGradientStart,
        Color(0xFF2E7D32), // Green
        Color(0xFF1565C0), // Blue
        Color(0xFFEF6C00), // Orange
        Color(0xFF455A64)  // Gray
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Card") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .androidx.compose.foundation.verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            Text("Card Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name (e.g. HDFC)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = last4Digits,
                onValueChange = { if (it.length <= 4) last4Digits = it },
                label = { Text("Last 4 Digits") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Card Color", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(colors) { color ->
                    val isSelected = color.toArgb() == selectedColor
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { selectedColor = color.toArgb() },
                        shape = CircleShape,
                        color = color,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null
                    ) {
                        if (isSelected) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = addInitialBill,
                    onCheckedChange = { addInitialBill = it }
                )
                Text("Add current bill statement?")
            }

            if (addInitialBill) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Total Due Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // TODO: Replace with DatePicker in production. For now, defaulting to today + 20 days if empty or parsing logic
                // Using a simple Day Input for simplicity in this prototype or just assuming user enters day
                // Let's implement a very simple DatePicker Dialog or just inputs for Day/Month
                // For this prototype, I'll assume Due Date is Today + 20 days if not complex
                // Let's just use a simple text field for "Days from now" to keep it safe?
                // No, let's use a default logic: If they add a bill, let's say "Due Date" is mandatory.
                // I'll add a simple "Due Date (DD/MM/YYYY)" text field.

                OutlinedTextField(
                    value = dueDateString,
                    onValueChange = { dueDateString = it },
                    label = { Text("Due Date (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (bankName.isNotBlank() && last4Digits.length == 4) {
                        val card = CardEntity(
                            bankName = bankName,
                            last4Digits = last4Digits,
                            colorArgb = selectedColor
                        )

                        var bill: BillEntity? = null
                        if (addInitialBill && amount.isNotBlank() && dueDateString.isNotBlank()) {
                            try {
                                val parts = dueDateString.split("/")
                                if (parts.size == 3) {
                                    val day = parts[0].toInt()
                                    val month = parts[1].toInt()
                                    val year = parts[2].toInt()
                                    val date = LocalDate.of(year, month, day)
                                    val epoch = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                                    bill = BillEntity(
                                        cardId = 0, // Will be set by ViewModel
                                        amount = amount.toDouble(),
                                        dueDate = epoch,
                                        isPaid = false
                                    )
                                }
                            } catch (e: Exception) {
                                // Ignore parsing error for prototype, maybe show toast
                            }
                        }

                        onSaveClick(card, bill)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = bankName.isNotBlank() && last4Digits.length == 4
            ) {
                Text("Save Card")
            }
        }
    }
}
