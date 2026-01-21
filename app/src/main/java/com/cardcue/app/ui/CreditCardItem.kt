package com.cardcue.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.ui.graphics.vector.ImageVector
import com.cardcue.app.model.CardUiState
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object IconUtils {
    fun getBankIcon(bankName: String): ImageVector {
        return when {
            bankName.contains("HDFC", true) -> Icons.Default.AccountBalance
            bankName.contains("ICICI", true) -> Icons.Default.CreditCard
            else -> Icons.Default.CreditCard
        }
    }
}

@Composable
fun CreditCardItem(
    cardState: CardUiState,
    onItemClick: () -> Unit
) {
    val card = cardState.card
    val bill = cardState.latestBill

    // Color Handling
    val cardColor = Color(card.colorArgb)
    // Create a simple gradient or solid color
    val brush = Brush.horizontalGradient(
        listOf(cardColor, cardColor.copy(alpha = 0.8f))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconUtils.getBankIcon(card.bankName),
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = card.bankName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "•••• ${card.last4Digits}",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // Status Badge
                    if (bill != null) {
                        if (bill.isPaid) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("PAID", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                             // Days Left Logic
                            val dueDate = Instant.ofEpochMilli(bill.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                            val today = java.time.LocalDate.now()
                            val daysLeft = ChronoUnit.DAYS.between(today, dueDate)

                             Text(
                                text = if (daysLeft < 0) "Overdue" else "$daysLeft days left",
                                color = if (daysLeft < 0) Color(0xFFFFCDD2) else Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (daysLeft < 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer Info
                if (bill != null) {
                    val formattedAmount = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(bill.amount)
                    val formattedDate = Instant.ofEpochMilli(bill.dueDate)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd MMM"))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Total Due",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = formattedAmount,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Due Date",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = formattedDate,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    // No Bill Set State
                    Button(
                        onClick = { onItemClick() }, // Reuse click which will open "Set Due" dialog
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set Due for Current Month")
                    }
                }
            }
        }
    }
}
