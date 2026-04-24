package com.example.fitrack.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitrack.ui.theme.TextDim
import kotlin.math.roundToInt

@Composable
fun MacroBar(
    label: String,
    value: Double,
    goal: Double,
    unit: String = "g",
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (value / goal).toFloat().coerceIn(0f, 1f) else 0f

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${value.roundToInt()} / ${goal.roundToInt()}$unit",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.06f)
        )
    }
}
