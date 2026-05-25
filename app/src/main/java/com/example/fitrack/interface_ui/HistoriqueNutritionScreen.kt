package com.example.fitrack.interface_ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitrack.ui.theme.*
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

    Scaffold(
        containerColor = DarkBG,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBG)
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRetour) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Historique",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBG),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("MOYENNE SUR 7 JOURS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextDim)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${avgKcal.roundToInt()} KCAL", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black), color = MintFit)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (groupedDays.isEmpty()) {
                    item {
                        Text("Aucun historique disponible", color = TextDim, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
                    }
                } else {
                    items(groupedDays, key = { it.first }) { (dayStart, label, totalCal) ->
                        Card(
                            onClick = { onOuvrirJour(dayStart) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBG),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(label, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${totalCal.roundToInt()} kcal", style = MaterialTheme.typography.labelSmall, color = TextFaint)
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextDim)
                            }
                        }
                    }
                }
            }
        }
    }
}