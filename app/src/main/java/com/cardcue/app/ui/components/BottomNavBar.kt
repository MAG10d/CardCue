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
import com.cardcue.app.ui.navigation.Screen

@Composable
fun BottomNavBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = selectedItem == Screen.Home.route,
            onClick = { onItemSelected(Screen.Home.route) },
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedItem == Screen.Calendar.route,
            onClick = { onItemSelected(Screen.Calendar.route) },
            icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Calendar") },
            label = { Text("Calendar") }
        )
        NavigationBarItem(
            selected = selectedItem == Screen.Settings.route,
            onClick = { onItemSelected(Screen.Settings.route) },
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}

@Preview
@Composable
fun BottomNavBarPreview() {
    BottomNavBar(selectedItem = Screen.Home.route, onItemSelected = {})
}
