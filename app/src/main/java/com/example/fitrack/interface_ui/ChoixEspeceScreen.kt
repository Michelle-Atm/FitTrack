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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.DarkText
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.AuthViewModel

private data class EspeceOption(
    val id: String,
    val emoji: String,
    val nom: String,
    val description: String,
    val trait: String
)

private val especes = listOf(
    EspeceOption("renard",   "🦊", "Renard",   "Curieux, adaptable, malin.",           "Agilité mentale"),
    EspeceOption("pingouin", "🐧", "Pingouin", "Persévérant, régulier, social.",        "Endurance sociale"),
    EspeceOption("panda",    "🐼", "Panda",    "Calme, équilibré, bienveillant.",       "Force tranquille"),
    EspeceOption("axolotl",  "🦎", "Axolotl",  "Régénérateur, unique, résilient.",      "Régénération"),
)

@Composable
fun ChoixEspeceScreen(
    authViewModel: AuthViewModel,
    onConfirme: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val utilisateur by authViewModel.utilisateurActuel.collectAsStateWithLifecycle()
    val isLoading = uiState is AuthViewModel.AuthUiState.Chargement

    var especeSelectionnee by remember {
        mutableStateOf(utilisateur?.avatarEspece?.ifBlank { null })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choisis ton espèce",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Ton avatar évoluera avec toi. Ce choix est permanent.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim,
            modifier = Modifier.padding(bottom = 28.dp)
        )

        especes.forEach { espece ->
            val selected = espece.id == especeSelectionnee
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) MintFit.copy(alpha = 0.08f) else CardBG)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) MintFit else Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { especeSelectionnee = espece.id }
                    .padding(16.dp)
                    .semantics { testTag = "espece_${espece.id}" },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(VioletFit.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = espece.emoji, fontSize = 36.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = espece.nom,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = espece.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                    Text(
                        text = "✦ ${espece.trait}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (selected) MintFit else TextDim
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MintFit),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = DarkText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val user = utilisateur ?: return@Button
                authViewModel.mettreAJourProfil(user.copy(avatarEspece = especeSelectionnee ?: "renard"))
                onConfirme()
            },
            enabled = especeSelectionnee != null && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .semantics { testTag = "espece_confirmer_btn" },
            colors = ButtonDefaults.buttonColors(
                containerColor = MintFit,
                disabledContainerColor = MintFit.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = DarkText, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = "Confirmer mon choix",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }
        }

        if (utilisateur?.avatarEspece?.isNotBlank() == true) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Espèce actuelle : ${utilisateur!!.avatarEspece}",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim
            )
        }
    }
}
