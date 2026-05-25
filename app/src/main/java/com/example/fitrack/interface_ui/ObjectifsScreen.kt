package com.example.fitrack.interface_ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.example.fitrack.components.ObjectifRow
import com.example.fitrack.components.SideQuestCard
import com.example.fitrack.model.Seance
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.ObjectifViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ObjectifsScreen(
    viewModel: ObjectifViewModel,
    userId: String
) {
    val objectifState by viewModel.objectifUiState.collectAsStateWithLifecycle()
    val sideQuestState by viewModel.sideQuestUiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.chargerObjectifJournalier(userId)
            viewModel.chargerSideQuests(userId)
        }
    }

    val dateLabel = SimpleDateFormat("EEEE dd MMMM · 'Semaine' w", Locale.FRENCH)
        .format(Date())
        .replaceFirstChar { it.uppercase() }

    Scaffold(
        containerColor = DarkBG,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBG)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextDim
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Objectifs du jour",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        ),
                        color = Color.White
                    )
                }

                val score = when (val s = objectifState) {
                    is ObjectifViewModel.ObjectifUiState.Succes -> {
                        val p = s.progression
                        ((p.progressionCalories + p.progressionPas + p.progressionSeances) / 3 * 500).roundToInt()
                    }
                    else -> 0
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBG)
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = VioletFit
                        )
                        Text(
                            text = "$score PTS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = VioletFit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = objectifState) {
                is ObjectifViewModel.ObjectifUiState.Chargement -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = MintFit) }
                }
                is ObjectifViewModel.ObjectifUiState.Succes -> {
                    val obj = state.progression.objectif

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBG),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            ObjectifRow(
                                icon = Icons.Filled.LocalFireDepartment,
                                iconColor = AmberFit,
                                label = "Calories",
                                value = obj.caloriesActuelles,
                                goal = obj.caloriesObjectif,
                                unit = "kcal",
                                barColor = MintFit
                            )
                            ObjectifRow(
                                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                                iconColor = MintFit,
                                label = "Pas",
                                value = obj.pasActuels,
                                goal = obj.pasObjectif,
                                unit = "pas",
                                barColor = MintFit
                            )
                            ObjectifRow(
                                icon = Icons.Filled.FitnessCenter,
                                iconColor = VioletFit,
                                label = "Séances",
                                value = obj.seancesEffectuees,
                                goal = obj.seancesObjectif,
                                unit = "séances",
                                barColor = MintFit
                            )
                        }
                    }

                    if (state.progression.objectif.seancesEffectuees == 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBG),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.03f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FitnessCenter,
                                        contentDescription = null,
                                        tint = TextDim,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "Aucune séance enregistrée",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Appuie sur \"Logger une séance\" pour commencer",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = TextDim,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                is ObjectifViewModel.ObjectifUiState.Erreur -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBG),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                    ) {
                        Text(
                            text = "Données temporairement indisponibles",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextDim,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (userId.isNotBlank()) {
                        val seance = Seance(
                            date = System.currentTimeMillis(),
                            type = "autre",
                            dureeMinutes = 30
                        )
                        viewModel.loggerSeance(seance, userId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, VioletFit)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = VioletFit,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Logger une séance",
                        color = VioletFit,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SIDE QUEST",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = TextDim
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (val sqState = sideQuestState) {
                is ObjectifViewModel.SideQuestUiState.Succes -> {
                    val activeQuest = sqState.utilisateur.firstOrNull { it.debloquee && !it.completee }
                    val questDef = activeQuest?.let { uq ->
                        sqState.disponibles.firstOrNull { it.id == uq.questId }
                    }
                    if (questDef != null) {
                        SideQuestCard(
                            active = true,
                            level = questDef.type.replaceFirstChar { it.uppercase() },
                            title = questDef.titre,
                            current = 1,
                            total = 3,
                            xp = questDef.xpRecompense
                        )
                    } else {
                        SideQuestCard(active = false)
                    }
                }
                else -> SideQuestCard(active = false)
            }
        }
    }
}