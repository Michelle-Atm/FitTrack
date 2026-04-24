package com.example.fitrack.interface_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitrack.components.HistoriqueRow
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.viewmodel.NutritionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HistoriqueNutritionScreen(
    viewModel: NutritionViewModel,
    userId: String,
    onRetour: () -> Unit,
    onOuvrirJour: (Long) -> Unit
) {
    val historique by viewModel.historiqueRepas.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) viewModel.chargerHistorique(userId, 7)
    }

    // Group repas by day and compute daily totals
    val dayFormat = SimpleDateFormat("EEEE dd MMMM", Locale.FRENCH)
    val groupedDays = historique
        .groupBy { viewModel.debutJournee(it.date) }
        .entries
        .sortedByDescending { it.key }
        .map { (dayStart, repas) ->
            val label = dayFormat.format(Date(dayStart)).replaceFirstChar { it.uppercase() }
            val totalCal = repas.sumOf { it.calories }
            Triple(dayStart, label, totalCal)
        }

    val avgKcal = if (groupedDays.isNotEmpty())
        groupedDays.sumOf { it.third } / groupedDays.size else 0.0
    val objectifRef = 2000.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onRetour,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f))
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White
                )
            }
            Text(
                text = "Historique",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Text(
            text = "7 derniers jours · moyenne ",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Text(
            text = "${avgKcal.roundToInt()} kcal",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (groupedDays.isEmpty()) {
                item {
                    Text(
                        text = "Aucun historique disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDim,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(groupedDays, key = { it.first }) { (dayStart, label, totalCal) ->
                    HistoriqueRow(
                        date = label,
                        kcal = totalCal,
                        goal = objectifRef,
                        onClick = { onOuvrirJour(dayStart) }
                    )
                }
            }
        }
    }
}
