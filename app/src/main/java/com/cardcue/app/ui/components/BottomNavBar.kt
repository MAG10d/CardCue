package com.cardcue.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /* No-op */ },
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* No-op */ },
            icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Calendar") },
            label = { Text("Calendar") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* No-op */ },
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}

@Preview
@Composable
fun BottomNavBarPreview() {
    BottomNavBar()
}
