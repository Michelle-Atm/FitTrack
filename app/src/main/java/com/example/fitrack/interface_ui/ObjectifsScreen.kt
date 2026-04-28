package com.example.fitrack.interface_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitrack.components.ObjectifRow
import com.example.fitrack.components.SideQuestCard
import com.example.fitrack.model.Seance
import com.example.fitrack.ui.theme.AmberFit
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

    val dateLabel = SimpleDateFormat("EEEE dd MMMM · 'semaine' w", Locale.FRENCH)
        .format(Date())
        .replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Objectifs du jour",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(VioletFit.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                        color = VioletFit
                    )
                    Text(
                        text = "$score pts",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = VioletFit
                    )
                }
            }
        }

        // Objectif rows
        when (val state = objectifState) {
            is ObjectifViewModel.ObjectifUiState.Chargement -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = MintFit) }
            }
            is ObjectifViewModel.ObjectifUiState.Succes -> {
                val obj = state.progression.objectif
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                if (state.progression.objectif.seancesEffectuees == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            tint = TextDim,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Aucune séance enregistrée",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDim
                        )
                        Text(
                            text = "Appuie sur \"Logger une séance\" pour commencer",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            is ObjectifViewModel.ObjectifUiState.Erreur -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Données temporairement indisponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDim
                    )
                }
            }
            else -> {}
        }

        // Logger séance button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
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
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, VioletFit)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = VioletFit,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Logger une séance",
                        color = VioletFit,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Side Quests
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp)
        ) {
            Text(
                text = "SIDE QUEST",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                modifier = Modifier.padding(bottom = 10.dp)
            )
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
