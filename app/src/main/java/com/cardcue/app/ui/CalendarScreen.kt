package com.cardcue.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cardcue.app.data.BillEntity
import com.cardcue.app.model.BillStatus
import com.cardcue.app.model.CreditCardBill
import com.cardcue.app.ui.components.BottomNavBar
import com.cardcue.app.ui.navigation.Screen
import com.cardcue.app.ui.theme.CalendarSelectedDate
import com.cardcue.app.ui.theme.CalendarSelectedDateText
import com.cardcue.app.ui.theme.PurpleGradientEnd
import com.cardcue.app.ui.theme.PurpleGradientStart
import com.cardcue.app.ui.theme.RedGradientEnd
import com.cardcue.app.ui.theme.RedGradientStart
import com.cardcue.app.ui.theme.StatTextPaid
import com.cardcue.app.ui.theme.StatTextLate
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: HomeViewModel,
    onBottomNavClick: (String) -> Unit
) {
    val billsEntities by viewModel.allBills.collectAsState()

    // Convert entities to logic friendly list
    val bills = billsEntities

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // Formatter
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")

    Scaffold(
        bottomBar = { BottomNavBar(selectedItem = Screen.Calendar.route, onItemSelected = onBottomNavClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header: Calendar Title + Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calendar",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                    }
                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Grid
            CalendarGrid(
                yearMonth = currentMonth,
                bills = bills,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Filtered List Header
            Text(
                text = if (selectedDate != null) "Due on ${selectedDate}" else "Bills in ${currentMonth.month}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Filtered List Logic
            val filteredBills = bills.filter { bill ->
                val billDate = Instant.ofEpochMilli(bill.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                if (selectedDate != null) {
                    billDate == selectedDate
                } else {
                    YearMonth.from(billDate) == currentMonth
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                items(filteredBills) { bill ->
                    // Map Entity to UI Model for reuse
                    // For now, we reuse CreditCardItem but need to adapt it to Entity or create a mapping
                    // Since CreditCardItem expects CreditCardBill (the UI model), we should map it.
                    val uiBill = CreditCardBill(
                        bankName = bill.bankName,
                        cardNumber = bill.cardNumber,
                        totalDue = "₹${bill.amount}",
                        minDue = "₹${bill.amount / 10}", // Mock calculation
                        dueDate = Instant.ofEpochMilli(bill.dueDate).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter),
                        dueDateIso = Instant.ofEpochMilli(bill.dueDate).atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                        daysLeft = 0, // Logic omitted for brevity
                        cardColor = if (bill.bankName.contains("HDFC")) listOf(RedGradientStart, RedGradientEnd) else listOf(PurpleGradientStart, PurpleGradientEnd), // Simple logic
                        status = if (bill.isPaid) BillStatus.PAID else BillStatus.DUE,
                        logoResId = 1 // Default icon
                    )

                    CreditCardItem(
                        bill = uiBill,
                        onItemClick = {} // Placeholder or navigation logic
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    bills: List<BillEntity>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val startOffset = if (yearMonth.atDay(1).dayOfWeek == DayOfWeek.SUNDAY) 0 else yearMonth.atDay(1).dayOfWeek.value

    val days = (1..daysInMonth).toList()

    Column {
        // Days of Week Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
            weekDays.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp),
            userScrollEnabled = false
        ) {
            items(startOffset) {
                Box(modifier = Modifier.aspectRatio(1f))
            }

            items(days) { day ->
                val date = yearMonth.atDay(day)
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()

                // Find bills for this day
                val billsOnDate = bills.filter {
                    Instant.ofEpochMilli(it.dueDate).atZone(ZoneId.systemDefault()).toLocalDate() == date
                }
                val hasUnpaid = billsOnDate.any { !it.isPaid }
                val hasPaid = billsOnDate.any { it.isPaid }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CalendarSelectedDate else Color.Transparent)
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.toString(),
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) CalendarSelectedDateText else Color.Black
                        )

                        if (billsOnDate.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                if (hasUnpaid) {
                                    Canvas(modifier = Modifier.size(6.dp)) {
                                        drawCircle(color = StatTextLate)
                                    }
                                }
                                if (hasPaid) {
                                    Canvas(modifier = Modifier.size(6.dp)) {
                                        drawCircle(color = StatTextPaid)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
