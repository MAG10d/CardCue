package com.cardcue.app.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String, val arguments: List<NamedNavArgument> = emptyList()) {
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
    object AddBill : Screen("add_bill")

    object BillDetail : Screen(
        route = "bill_detail/{billId}",
        arguments = listOf(navArgument("billId") { type = NavType.IntType })
    ) {
        fun createRoute(billId: Int) = "bill_detail/$billId"
    }

    object EditBill : Screen(
        route = "edit_bill/{billId}",
        arguments = listOf(navArgument("billId") { type = NavType.IntType })
    ) {
        fun createRoute(billId: Int) = "edit_bill/$billId"
    }
}
