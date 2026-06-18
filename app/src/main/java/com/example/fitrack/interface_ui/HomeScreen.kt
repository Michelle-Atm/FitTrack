package com.example.fitrack.interface_ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitrack.model.User
import com.example.fitrack.navigation.ROUTE_GPS
import com.example.fitrack.navigation.ROUTE_LEADERBOARD
import com.example.fitrack.navigation.ROUTE_NUTRITION
import com.example.fitrack.navigation.ROUTE_OBJECTIFS
import com.example.fitrack.navigation.ROUTE_PODOMETRE
import com.example.fitrack.navigation.ROUTE_PROFIL
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.CardBG2
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.DarkText
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.utils.emojiAnimal
import com.example.fitrack.viewmodel.ObjectifViewModel

@Composable
fun HomeScreen(
    user: User?,
    navController: NavController,
    objectifViewModel: ObjectifViewModel
) {
    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBG),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = VioletFit)
        }
        return
    }

    LaunchedEffect(user.uid) {
        objectifViewModel.chargerObjectifJournalier(user.uid)
    }

    val objectifState by objectifViewModel.objectifUiState.collectAsStateWithLifecycle()
    val progression = (objectifState as? ObjectifViewModel.ObjectifUiState.Succes)?.progression

    val xpDansNiveau = user.xp % 300
    val xpProgressionCible = xpDansNiveau.toFloat() / 300f
    val xpProgression by animateFloatAsState(
        targetValue = xpProgressionCible,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "xp_progress"
    )

    val rangTitre = when (user.niveau) {
        in 1..3 -> "Recrue Fitness"
        in 4..7 -> "Athlète Régulier"
        in 8..11 -> "Guerrier d'Acier"
        else -> "Légende Vivante"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AmberFit.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("🔥", fontSize = 12.sp)
                Text(
                    text = "${user.streakJours} jours de streak",
                    color = AmberFit,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate(ROUTE_PROFIL) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, VioletFit, CircleShape)
                    .background(CardBG),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(CardBG2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emojiAnimal(user.avatarEspece),
                        fontSize = 32.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(4.dp))
                        .background(VioletFit)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Lv${user.niveau}",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bonjour, ${user.nom.ifBlank { user.email.substringBefore("@") }}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.semantics { testTag = "home_greeting" }
                )
                Text(
                    text = "Rang : $rangTitre",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextBadge(text = "Top 8%", color = MintFit)
                    TextBadge(text = "+250 XP", color = VioletFit)
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardBG)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                    .clickable { navController.navigate(ROUTE_PROFIL) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil",
                    tint = VioletFit,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "XP vers Lv${user.niveau + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
                Text(
                    text = "$xpDansNiveau / 300 XP",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = VioletFit
                )
            }
            LinearProgressIndicator(
                progress = { xpProgression },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = VioletFit,
                trackColor = CardBG
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "DÉFIS DU JOUR",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim,
            modifier = Modifier.fillMaxWidth()
        )

        val obj = progression?.objectif
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBG),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChallengeRow(
                    title = "Calories consommées",
                    current = obj?.caloriesActuelles?.toInt() ?: 0,
                    target = obj?.caloriesObjectif?.toInt() ?: 2000,
                    unit = "kcal",
                    progress = progression?.progressionCalories ?: 0f,
                    xp = "+80 XP",
                    icon = Icons.Default.LocalFireDepartment,
                    color = MintFit
                ) {
                    navController.navigate(ROUTE_NUTRITION)
                }

                androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                ChallengeRow(
                    title = "Pas quotidiens",
                    current = obj?.pasActuels ?: 0,
                    target = obj?.pasObjectif ?: 10000,
                    unit = "pas",
                    progress = progression?.progressionPas ?: 0f,
                    xp = "+120 XP",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    color = VioletFit
                ) {
                    navController.navigate(ROUTE_PODOMETRE)
                }

                androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                ChallengeRow(
                    title = "Séances de sport",
                    current = obj?.seancesEffectuees ?: 0,
                    target = obj?.seancesObjectif ?: 1,
                    unit = "séance" + (if ((obj?.seancesObjectif ?: 1) > 1) "s" else ""),
                    progress = progression?.progressionSeances ?: 0f,
                    xp = "+200 XP",
                    icon = Icons.Default.FitnessCenter,
                    color = AmberFit
                ) {
                    navController.navigate(ROUTE_OBJECTIFS)
                }
            }
        }

        Text(
            text = "OUTILS DE SUIVI",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RaccourciCard(
                label = "Podomètre",
                icone = Icons.AutoMirrored.Filled.DirectionsWalk,
                accent = MintFit,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(ROUTE_PODOMETRE) }
            RaccourciCard(
                label = "GPS",
                icone = Icons.Default.Map,
                accent = AmberFit,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(ROUTE_GPS) }
            RaccourciCard(
                label = "Avatar",
                icone = Icons.Default.Pets,
                accent = VioletFit,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(com.example.fitrack.navigation.ROUTE_AVATAR) }
        }

        Text(
            text = "BADGES RÉCENTS",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val aFaitSeance = (progression?.objectif?.seancesEffectuees ?: 0) > 0
            BadgeCard(emoji = "🏋️", label = "Force Max", unlocked = aFaitSeance)
            BadgeCard(emoji = "🔥", label = "10 Jours", unlocked = user.streakJours >= 10)
            BadgeCard(emoji = "⚡", label = "Sprint Pro", unlocked = user.niveau >= 5)
            BadgeCard(emoji = "👑", label = "Légende", unlocked = user.niveau >= 10)
        }
    }
}

@Composable
private fun TextBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChallengeRow(
    title: String,
    current: Int,
    target: Int,
    unit: String,
    progress: Float,
    xp: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "$current / $target $unit",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = CardBG2
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(VioletFit.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = xp,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = VioletFit
            )
        }
    }
}

@Composable
private fun BadgeCard(
    emoji: String,
    label: String,
    unlocked: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CardBG)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .then(if (!unlocked) Modifier.graphicsLayer(alpha = 0.3f) else Modifier)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (unlocked) Color.White else TextDim,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RaccourciCard(
    label: String,
    icone: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

