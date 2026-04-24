package com.example.fitrack.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.Border
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DangerFit
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.TextFaint
import kotlin.math.roundToInt

@Composable
fun HistoriqueRow(
    date: String,
    kcal: Double,
    goal: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pct = if (goal > 0) (kcal / goal * 100).roundToInt().coerceIn(0, 200) else 0
    val barColor = when {
        pct >= 80 -> MintFit
        pct >= 50 -> AmberFit
        else -> DangerFit
    }
    val progressFraction = if (goal > 0) (kcal / goal).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBG),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${kcal.roundToInt()}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = barColor
                        )
                        Text(
                            text = "/ ${goal.roundToInt()} kcal · $pct%",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = TextFaint,
                    modifier = Modifier.size(18.dp)
                )
            }
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = barColor,
                trackColor = Color.White.copy(alpha = 0.06f)
            )
        }
    }
}
