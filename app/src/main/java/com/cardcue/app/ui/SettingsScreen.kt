package com.cardcue.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onBackClick: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val financialProfile by viewModel.financialProfile.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState)
        ) {
            // Financial Profile Section
            Text(
                text = "Financial Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = if (financialProfile.salary == 0.0) "" else financialProfile.salary.toString(),
                onValueChange = {
                    val salary = it.toDoubleOrNull() ?: 0.0
                    viewModel.setSalary(salary)
                },
                label = { Text("Monthly Salary") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = financialProfile.payday.toString(),
                onValueChange = {
                    val day = it.toIntOrNull()
                    if (day != null && day in 1..31) {
                        viewModel.setPayday(day)
                    }
                },
                label = { Text("Payday (Day of Month)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Appearance Section
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingToggleRow(
                title = "Use System Colors",
                description = "Match your wallpaper (Material You)",
                isChecked = useDynamicColors,
                onCheckedChange = { viewModel.setDynamicColors(it) }
            )

            SettingToggleRow(
                title = "Dark Mode",
                description = "Enable dark theme for the app",
                isChecked = isDarkMode,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Security Section
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingToggleRow(
                title = "Biometric Lock",
                description = "Unlock app using fingerprint/face",
                isChecked = isBiometricEnabled,
                onCheckedChange = { viewModel.setBiometricEnabled(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Data Section (Deprecated/Placeholder)
            Text(
                text = "Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val context = androidx.compose.ui.platform.LocalContext.current

            androidx.compose.material3.Button(
                onClick = { viewModel.exportData(context, android.net.Uri.EMPTY) }, // URI handling mocked for now as logic is disabled
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Disabled as per plan
            ) {
                Text("Export to JSON (Coming Soon)")
            }

            androidx.compose.material3.OutlinedButton(
                onClick = { viewModel.importData(context, android.net.Uri.EMPTY) }, // URI handling mocked
                modifier = Modifier.fillMaxWidth(),
                enabled = false // Disabled as per plan
            ) {
                Text("Import from JSON (Coming Soon)")
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
