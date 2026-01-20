package com.cardcue.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cardcue.app.model.BillStatus
import com.cardcue.app.model.CreditCardBill
import com.cardcue.app.ui.theme.DaysLeftTagBackground
import com.cardcue.app.ui.theme.DaysLeftTagText
import com.cardcue.app.ui.theme.RedGradientEnd
import com.cardcue.app.ui.theme.RedGradientStart
import com.cardcue.app.ui.theme.StatTextPaid

@Composable
fun CreditCardItem(
    bill: CreditCardBill,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = bill.cardColor
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Header: Bank Icon + Name + Masked Card + Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Use ImageVector.vectorResource if it was a drawable, but here we use Icon with vector directly?
                        // The model has logoResId which is Int. We can load it via painterResource(id = bill.logoResId) or ImageVector.vectorResource
                        // But wait, the previous instruction said "map them to standard Material Icons".
                        // Standard Material Icons are ImageVectors, not Int resource IDs usually, unless we wrap them.
                        // Let's assume for this specific component we might need to handle the Int as a resource ID.
                        // However, to make it compatible with "Icons.Default.X" which are ImageVectors,
                        // we might need to change the model to store ImageVector OR resolve the Int to Vector.
                        // Since the user asked for "Int resource IDs", I will assume we are passing R.drawable.x or we need a way to resolve it.
                        // But wait, the user said "map them to standard Material Icons... e.g. Icons.Default.AccountBalance".
                        // Icons.Default.AccountBalance is an ImageVector object, it doesn't have a stable Int ID we can easily store in a data class unless we create a mapping.
                        // To allow the data class to hold "Int" as requested but use Vectors, I'll use a wrapper or just use the Int to lookup a Vector.
                        // FOR SIMPLICITY: I will interpret "Logo URL... String (or Int...)" as strict instructions.
                        // BUT "Use Icons.Default..." implies objects.
                        // I will assume `logoResId` is an Int, but for the mock data I will need to pass an Int that represents the Icon.
                        // Actually, it's easier to just store `imageVector: ImageVector` in the model for the purpose of this mock app,
                        // BUT the requirement was "Int resource ID".
                        // Okay, I will implement a helper to map an arbitrary Int ID to an ImageVector for now.

                        val iconVector = getIconForId(bill.logoResId)

                        Icon(
                            imageVector = iconVector,
                            contentDescription = "Bank Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = bill.bankName,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•••• ${bill.cardNumber}",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Days Left Tag
                    Box(
                        modifier = Modifier
                            .background(
                                color = DaysLeftTagBackground,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${bill.daysLeft} days left",
                            color = DaysLeftTagText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Middle: Total Due + Minimum Due
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Due",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = bill.totalDue,
                            color = if (bill.status == BillStatus.PAID) StatTextPaid else Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Minimum Due",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = bill.minDue,
                            color = if (bill.status == BillStatus.PAID) StatTextPaid else Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer: Due Date + Mark as Paid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Due Date",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = bill.dueDate,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { /* TODO: Handle payment */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Paid",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Mark as Paid")
                    }
                }
            }
        }
    }
}

// Temporary helper to map "IDs" to Vectors since we don't have drawable resources for them yet
// and the requirement was to use Int resource IDs but map to Material Icons.
// In a real app, these would be R.drawable.hdfc_logo etc.
fun getIconForId(id: Int): ImageVector {
    return when (id) {
        1 -> Icons.Default.AccountBox // Placeholder for AccountBalance if not available in Core
        2 -> Icons.Default.ShoppingCart
        3 -> Icons.Default.CreditCard
        else -> Icons.Default.CreditCard
    }
}

// Helper to provide easy access to "IDs" for mock data
object BankIcons {
    const val HDFC = 1
    const val INDUSIND = 2
    const val ICICI = 3
}

@Preview
@Composable
fun CreditCardItemPreview() {
    val sampleBill = CreditCardBill(
        bankName = "HDFC Bank",
        cardNumber = "4582",
        totalDue = "₹12,450",
        minDue = "₹850",
        dueDate = "24 Aug",
        dueDateIso = "2025-08-24",
        daysLeft = 5,
        cardColor = listOf(RedGradientStart, RedGradientEnd),
        status = BillStatus.DUE,
        logoResId = BankIcons.HDFC
    )
    CreditCardItem(bill = sampleBill)
}
