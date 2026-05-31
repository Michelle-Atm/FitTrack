package com.example.fitrack.interface_ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.fitrack.model.Avatar
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DangerFit
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.AvatarViewModel

@Composable
fun AvatarScreen(
    avatarViewModel: AvatarViewModel,
    userId: String
) {
    val avatar by avatarViewModel.avatar.collectAsStateWithLifecycle()
    val xpProgression by avatarViewModel.xpProgression.collectAsStateWithLifecycle()
    val prochainNiveau by avatarViewModel.prochainNiveau.collectAsStateWithLifecycle()
    val xpPourNiveau by avatarViewModel.xpPourNiveau.collectAsStateWithLifecycle()
    val historiqueEvolution by avatarViewModel.historiqueEvolution.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            avatarViewModel.chargerAvatar(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mon Avatar",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Surface(
                color = VioletFit.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, VioletFit.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "NIV. ${avatar?.niveau ?: 1}",
                    color = VioletFit,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(couleurEtat(avatar?.etatActuel).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emojiEspece(avatar?.espece),
                fontSize = 80.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Surface(
            color = couleurEtat(avatar?.etatActuel).copy(alpha = 0.12f),
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, couleurEtat(avatar?.etatActuel))
        ) {
            Text(
                text = etatLabel(avatar?.etatActuel),
                color = couleurEtat(avatar?.etatActuel),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progression vers niv. $prochainNiveau",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
                Text(
                    text = "${avatar?.xpCumule ?: 0} / $xpPourNiveau XP",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = VioletFit
                )
            }
            LinearProgressIndicator(
                progress = { xpProgression },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = VioletFit,
                trackColor = CardBG
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "NIVEAUX",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NiveauRow(
                numero = 1, label = "Niv. 1 — 0 à 30 jours",
                atteint = (avatar?.niveau ?: 1) >= 1
            )
            NiveauRow(
                numero = 2, label = "Niv. 2 — 30 à 90 jours",
                atteint = (avatar?.niveau ?: 1) >= 2
            )
            NiveauRow(
                numero = 3, label = "Niv. 3 — 90 à 180 jours",
                atteint = (avatar?.niveau ?: 1) >= 3
            )
            NiveauRow(
                numero = 4, label = "Niv. 4 — 180+ jours",
                atteint = (avatar?.niveau ?: 1) >= 4
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "PERSONNALITÉ",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBG),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = emojiEspece(avatar?.espece), fontSize = 28.sp)
                Column {
                    Text(
                        text = especeNom(avatar?.espece),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = especeDescription(avatar?.espece),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }
        }

        if (historiqueEvolution.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "HISTORIQUE",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                historiqueEvolution.forEach { evenement ->
                    Text(
                        text = "• $evenement",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }
        }
    }
}

@Composable
private fun NiveauRow(numero: Int, label: String, atteint: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (atteint) MintFit.copy(alpha = 0.06f) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (atteint) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = null,
            tint = if (atteint) MintFit else TextDim,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (atteint) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (atteint) Color.White else TextDim,
            modifier = Modifier.weight(1f)
        )
        if (atteint) {
            Text("✓", color = MintFit, fontWeight = FontWeight.Bold)
        } else {
            Text("🔒", fontSize = 14.sp)
        }
    }
}

private fun couleurEtat(etat: String?): Color = when (etat) {
    "champion" -> MintFit
    "heureux"  -> MintFit.copy(alpha = 0.7f)
    "neutre"   -> AmberFit
    "triste"   -> DangerFit
    else       -> TextDim
}

private fun etatLabel(etat: String?): String = when (etat) {
    "champion" -> "Champion"
    "heureux"  -> "Heureux"
    "neutre"   -> "Neutre"
    "triste"   -> "Triste"
    else       -> "—"
}

private fun emojiEspece(espece: String?): String = when (espece) {
    "renard"   -> "🦊"
    "pingouin" -> "🐧"
    "panda"    -> "🐼"
    "axolotl"  -> "🦎"
    else       -> "🐾"
}

private fun especeNom(espece: String?): String = when (espece) {
    "renard"   -> "Renard"
    "pingouin" -> "Pingouin"
    "panda"    -> "Panda"
    "axolotl"  -> "Axolotl"
    else       -> "Animal mystérieux"
}

private fun especeDescription(espece: String?): String = when (espece) {
    "renard"   -> "Curieux, adaptable, malin."
    "pingouin" -> "Persévérant, régulier, social."
    "panda"    -> "Calme, équilibré, bienveillant."
    "axolotl"  -> "Régénérateur, unique, résilient."
    else       -> "Espèce inconnue."
}