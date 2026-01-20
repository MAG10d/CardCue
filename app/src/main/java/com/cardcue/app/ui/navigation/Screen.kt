package com.cardcue.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Calendar : Screen("calendar")
    data object Settings : Screen("settings")
    data object AddBill : Screen("add_bill")
}
