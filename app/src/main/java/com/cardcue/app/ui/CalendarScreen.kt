package com.cardcue.app.ui

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.cardcue.app.model.CreditCardBill
import com.cardcue.app.ui.components.BottomNavBar
import com.cardcue.app.ui.navigation.Screen
import com.cardcue.app.ui.theme.CalendarSelectedDate
import com.cardcue.app.ui.theme.CalendarSelectedDateText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    bills: List<CreditCardBill>,
    onBottomNavClick: (String) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.of(2025, 7)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val today = LocalDate.now()

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                    }
                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
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

            // Filtered List
            val filteredBills = bills.filter { bill ->
                try {
                    val billDate = LocalDate.parse(bill.dueDateIso)
                    if (selectedDate != null) {
                        billDate == selectedDate
                    } else {
                        YearMonth.from(billDate) == currentMonth
                    }
                } catch (e: Exception) {
                    false
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                items(filteredBills) { bill ->
                    CreditCardItem(bill = bill)
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    bills: List<CreditCardBill>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value % 7 // Sunday = 0, Monday = 1... wait. DayOfWeek.MONDAY.value is 1. Sunday is 7.
    // We want Sunday to be the first column.
    // If DayOfWeek is MON(1), we want index 1.
    // If DayOfWeek is SUN(7), we want index 0.
    val startOffset = if (yearMonth.atDay(1).dayOfWeek == DayOfWeek.SUNDAY) 0 else yearMonth.atDay(1).dayOfWeek.value

    val days = (1..daysInMonth).toList()
    val totalCells = startOffset + daysInMonth

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
            modifier = Modifier.height(300.dp), // Fixed height for grid
            userScrollEnabled = false
        ) {
            // Empty cells for offset
            items(startOffset) {
                Box(modifier = Modifier.aspectRatio(1f))
            }

            // Day cells
            items(days) { day ->
                val date = yearMonth.atDay(day)
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()
                val billsOnDate = bills.filter {
                    try { LocalDate.parse(it.dueDateIso) == date } catch (e: Exception) { false }
                }

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
                            // Show small icon for the first bill
                            val icon = getIconForId(billsOnDate.first().logoResId)
                            Icon(
                                imageVector = icon,
                                contentDescription = "Bill Due",
                                tint = if (isSelected) CalendarSelectedDateText else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
