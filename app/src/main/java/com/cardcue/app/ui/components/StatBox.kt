package com.cardcue.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cardcue.app.ui.theme.StatBoxSelected
import com.cardcue.app.ui.theme.StatTextLate

@Composable
fun StatBox(
    count: String,
    label: String,
    isSelected: Boolean = false,
    textColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) StatBoxSelected else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .size(80.dp), // Ensure column fills the card size for alignment
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.Black else Color.Gray
            )
        }
    }
}

@Preview
@Composable
fun StatBoxPreview() {
    StatBox(count = "4", label = "Total", isSelected = true)
}

@Preview
@Composable
fun StatBoxLatePreview() {
    StatBox(count = "0", label = "Late", textColor = StatTextLate)
}
