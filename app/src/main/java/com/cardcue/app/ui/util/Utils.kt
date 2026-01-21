package com.cardcue.app.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormattingUtils {
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")) // Assuming INR based on request examples
        return format.format(amount)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

object IconUtils {
    fun getBankIcon(bankName: String): ImageVector {
        return when {
            bankName.contains("HDFC", ignoreCase = true) -> Icons.Default.AccountBalance
            bankName.contains("ICICI", ignoreCase = true) -> Icons.Default.CreditCard
            bankName.contains("IndusInd", ignoreCase = true) -> Icons.Default.AccountBalance
            else -> Icons.Default.CreditCard
        }
    }
}
