package com.example.fitrack.interface_ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitrack.components.CelebrationOverlay
import com.example.fitrack.components.ObjectifRow
import com.example.fitrack.components.SideQuestCard
import com.example.fitrack.model.Seance
import com.example.fitrack.model.User
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectifsScreen(
    viewModel: ObjectifViewModel,
    userId: String,
    user: User? = null
) {
    val objectifState by viewModel.objectifUiState.collectAsStateWithLifecycle()
    val sideQuestState by viewModel.sideQuestUiState.collectAsStateWithLifecycle()

    var showSeanceSheet by remember { mutableStateOf(false) }
    var showScoreDetail by remember { mutableStateOf(false) }
    var celebrationMessage by remember { mutableStateOf("") }
    var showCelebration by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.chargerObjectifJournalier(userId)
            viewModel.chargerSideQuests(userId)
        }
    }

    LaunchedEffect(user) {
        if (user != null && userId.isNotBlank()) {
            viewModel.debloquerSideQuestsEligibles(userId, user)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.celebrationEvent.collect { message ->
            celebrationMessage = message
            showCelebration = true
        }
    }

    val dateLabel = SimpleDateFormat("EEEE dd MMMM · 'semaine' w", Locale.FRENCH)
        .format(Date())
        .replaceFirstChar { it.uppercase() }

    val progression = (objectifState as? ObjectifViewModel.ObjectifUiState.Succes)?.progression

    val scoreJour = progression?.let {
        ((it.progressionCalories + it.progressionPas + it.progressionSeances) / 3 * 1000).roundToInt()
    } ?: 0

    val seancesEffectuees = progression?.objectif?.seancesEffectuees ?: 0
    val streak = seancesEffectuees.coerceAtMost(7)
    val bonusStreak = streak * 10

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
                        text = "$scoreJour pts",
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

        // Score + Streak cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ScoreCard(
                label = "Score du jour",
                valeur = "$scoreJour",
                unite = "/ 1000",
                accent = VioletFit,
                modifier = Modifier.weight(1f)
            )
            ScoreCard(
                label = "Streak",
                valeur = "$streak",
                unite = "jours 🔥",
                accent = AmberFit,
                modifier = Modifier.weight(1f)
            )
        }

        // Détail score (accordéon)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { showScoreDetail = !showScoreDetail }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Détail du score", color = TextDim, style = MaterialTheme.typography.bodySmall)
            Icon(
                imageVector = if (showScoreDetail) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = TextDim,
                modifier = Modifier.size(18.dp)
            )
        }
        AnimatedVisibility(showScoreDetail) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DetailLigne("Score calories", "%.0f%%".format((progression?.progressionCalories ?: 0f) * 100))
                DetailLigne("Score pas", "%.0f%%".format((progression?.progressionPas ?: 0f) * 100))
                DetailLigne("Bonus streak", "+$bonusStreak XP")
                DetailLigne("Score séances", "%.0f%%".format((progression?.progressionSeances ?: 0f) * 100))
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
                    modifier = Modifier.padding(horizontal = 20.dp),
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
                    horizontalAlignment = Alignment.CenterHorizontally
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

        // Bouton Logger séance (ouvre BottomSheet)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { showSeanceSheet = true },
                modifier = Modifier.semantics { testTag = "objectifs_logger_btn" },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, VioletFit)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
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

    CelebrationOverlay(
        visible = showCelebration,
        message = celebrationMessage,
        onDismiss = { showCelebration = false }
    )

    // BottomSheet saisie séance
    if (showSeanceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSeanceSheet = false },
            sheetState = sheetState,
            containerColor = CardBG
        ) {
            SeanceBottomSheet(
                userId = userId,
                poidsKg = user?.poids ?: 70.0,
                onValider = { seance ->
                    viewModel.loggerSeance(seance, userId)
                    showSeanceSheet = false
                },
                onDismiss = { showSeanceSheet = false }
            )
        }
    }
}

@Composable
private fun SeanceBottomSheet(
    userId: String,
    poidsKg: Double = 70.0,
    onValider: (Seance) -> Unit,
    onDismiss: () -> Unit
) {
    val activites = listOf(
        "Marche rapide" to 3.5,
        "Jogging"       to 7.0,
        "Course"        to 8.5,
        "Musculation légère"  to 4.0,
        "Musculation intense" to 6.0,
        "HIIT"          to 10.0,
        "Yoga"          to 2.5,
        "Natation"      to 6.0
    )

    var activiteIndex by remember { mutableIntStateOf(0) }
    var dureeMinutes by remember { mutableFloatStateOf(30f) }

    val (nomActivite, met) = activites[activiteIndex]
    val calories = (met * poidsKg * (dureeMinutes / 60.0)).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Logger une séance",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        // Sélecteur type d'activité
        Text("Type d'activité", style = MaterialTheme.typography.labelSmall, color = TextDim)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activites.forEachIndexed { index, (nom, _) ->
                if (index < 4) {
                    val selected = activiteIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) VioletFit.copy(alpha = 0.2f) else DarkBG)
                            .clickable { activiteIndex = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nom.split(" ").first(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) VioletFit else TextDim,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activites.forEachIndexed { index, (nom, _) ->
                if (index >= 4) {
                    val selected = activiteIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) VioletFit.copy(alpha = 0.2f) else DarkBG)
                            .clickable { activiteIndex = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nom.split(" ").first(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) VioletFit else TextDim,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Durée
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Durée", style = MaterialTheme.typography.labelSmall, color = TextDim)
            Text(
                text = "${dureeMinutes.roundToInt()} min",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
        Slider(
            value = dureeMinutes,
            onValueChange = { dureeMinutes = it },
            valueRange = 10f..120f,
            steps = 21,
            colors = SliderDefaults.colors(
                thumbColor = VioletFit,
                activeTrackColor = VioletFit,
                inactiveTrackColor = CardBG
            )
        )

        // Score estimé
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(VioletFit.copy(alpha = 0.08f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Score estimé", color = TextDim, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "~$calories kcal",
                color = VioletFit,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = {
                val seance = Seance(
                    date = System.currentTimeMillis(),
                    userId = userId,
                    type = nomActivite.lowercase(),
                    dureeMinutes = dureeMinutes.roundToInt(),
                    caloriesDepensees = calories.toDouble()
                )
                onValider(seance)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VioletFit),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Valider", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ScoreCard(
    label: String,
    valeur: String,
    unite: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextDim)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = valeur,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    ),
                    color = accent
                )
                Text(
                    text = unite,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailLigne(label: String, valeur: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextDim)
        Text(valeur, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}
