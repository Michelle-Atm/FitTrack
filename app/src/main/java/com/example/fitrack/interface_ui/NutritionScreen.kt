package com.example.fitrack.interface_ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.fitrack.components.MacroBar
import com.example.fitrack.components.ProgressRing
import com.example.fitrack.components.RepasItem
import com.example.fitrack.model.HeureRepas
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.TextFaint
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.NutritionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel,
    userId: String,
    dateOverride: Long? = null,
    onAjouterRepas: () -> Unit,
    onHistorique: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val readOnly = dateOverride != null
    val targetDate = dateOverride ?: viewModel.debutJournee()

    LaunchedEffect(userId, targetDate) {
        if (userId.isNotBlank()) viewModel.chargerRepasJournaliers(userId, targetDate)
    }

    val dateLabel = if (readOnly) {
        SimpleDateFormat("EEEE dd MMMM", Locale.FRENCH).format(Date(targetDate))
            .replaceFirstChar { it.uppercase() }
    } else {
        SimpleDateFormat("EEE. dd MMMM", Locale.FRENCH).format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        containerColor = DarkBG,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (!readOnly) {
                FloatingActionButton(
                    onClick = onAjouterRepas,
                    containerColor = MintFit,
                    contentColor = Color(0xFF002817),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Ajouter un repas",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBG)
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dateLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextDim
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (readOnly) dateLabel else "Aujourd'hui",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp
                            ),
                            color = Color.White
                        )
                    }
                    if (!readOnly) {
                        IconButton(
                            onClick = onHistorique,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardBG)
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(14.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Historique",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            when (val state = uiState) {
                is NutritionViewModel.NutritionUiState.Chargement -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = MintFit) }
                }
                is NutritionViewModel.NutritionUiState.Erreur -> item {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                is NutritionViewModel.NutritionUiState.Succes -> {
                    val totaux = state.totaux
                    val comp = state.comparaison
                    val objectifCal = if (comp.pourcentageCalories > 0)
                        totaux.calories / comp.pourcentageCalories else 2000.0
                    val pct = (comp.pourcentageCalories * 100).roundToInt()

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBG),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ProgressRing(
                                    value = totaux.calories,
                                    goal = objectifCal,
                                    size = 200.dp,
                                    strokeWidth = 12.dp,
                                    color = MintFit
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "CALORIES",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = TextDim
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${totaux.calories.roundToInt()}",
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontSize = 38.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = (-1).sp
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "/ ${objectifCal.roundToInt()} kcal",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = TextDim
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "$pct% ATTEINT",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = MintFit
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBG),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val protGoal = if (comp.pourcentageProteines > 0)
                                    totaux.proteines / comp.pourcentageProteines else 150.0
                                val glucGoal = if (comp.pourcentageGlucides > 0)
                                    totaux.glucides / comp.pourcentageGlucides else 250.0
                                val lipGoal = if (comp.pourcentageLipides > 0)
                                    totaux.lipides / comp.pourcentageLipides else 65.0

                                MacroBar(label = "Protéines", value = totaux.proteines, goal = protGoal, color = MintFit)
                                MacroBar(label = "Glucides", value = totaux.glucides, goal = glucGoal, color = VioletFit)
                                MacroBar(label = "Lipides", value = totaux.lipides, goal = lipGoal, color = AmberFit)
                                MacroBar(label = "Fibres", value = totaux.fibres, goal = 30.0, color = Color(0xFF8C8CA8))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Repas du jour",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            if (state.repas.isNotEmpty()) {
                                Text(
                                    text = "${state.repas.size} items · swipe ←",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = TextFaint
                                )
                            }
                        }
                    }

                    if (state.repas.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBG),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                            ) {
                                Text(
                                    text = "Aucun repas enregistré",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextFaint,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(state.repas, key = { it.id }) { repas ->
                            RepasItem(
                                time = repas.heure,
                                meal = HeureRepas.fromValeur(repas.heure).name
                                    .replace("_", " ")
                                    .lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                name = repas.nom,
                                kcal = repas.calories.roundToInt(),
                                macros = "P ${repas.proteines.roundToInt()} · G ${repas.glucides.roundToInt()} · L ${repas.lipides.roundToInt()}",
                                onDelete = { viewModel.supprimerRepas(repas.id, userId) },
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}