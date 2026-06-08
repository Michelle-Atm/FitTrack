package com.example.fitrack.interface_ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.SocialViewModel

@Composable
fun LeaderboardScreen(
    socialViewModel: SocialViewModel,
    userId: String = "",
    categorie: String = "debutant",
    onClickProfil: ((String) -> Unit)? = null
) {
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) socialViewModel.chargerClassement(userId, categorie)
    }

    val maCategorie by socialViewModel.maCategorie.collectAsStateWithLifecycle()
    val monRang by socialViewModel.monRang.collectAsStateWithLifecycle()
    val monScoreHebdo by socialViewModel.monScoreHebdo.collectAsStateWithLifecycle()
    val classementGlobal by socialViewModel.classementGlobal.collectAsStateWithLifecycle()
    val classementCategorie by socialViewModel.classementCategorie.collectAsStateWithLifecycle()

    var tabSelectionne by remember { mutableIntStateOf(0) }
    val classement = if (tabSelectionne == 0) classementGlobal else classementCategorie

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
    ) {
        // Header
        Text(
            text = "Classement",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 12.dp)
        )

        // Tabs
        TabRow(
            selectedTabIndex = tabSelectionne,
            containerColor = CardBG,
            contentColor = MintFit
        ) {
            Tab(
                selected = tabSelectionne == 0,
                onClick = { tabSelectionne = 0 },
                text = { Text("Général") }
            )
            Tab(
                selected = tabSelectionne == 1,
                onClick = { tabSelectionne = 1 },
                text = { Text("Ma catégorie") }
            )
        }

        if (tabSelectionne == 1) {
            Text(
                text = "Catégorie : $maCategorie",
                color = AmberFit,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        } else {
            Spacer(Modifier.height(6.dp))
        }

        // Ma position sticky
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = VioletFit.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, VioletFit.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "#$monRang",
                    color = VioletFit,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.width(52.dp)
                )
                Text(
                    text = "Moi",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "%.0f pts".format(monScoreHebdo),
                    color = VioletFit,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            classement.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MintFit)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Podium top 3
                    if (classement.size >= 3) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PodiumItem(
                                    rang = 2, profil = classement[1], hauteur = 80.dp,
                                    onClick = if (classement[1].userId != socialViewModel.monUserId)
                                        { { onClickProfil?.invoke(classement[1].userId) } } else null
                                )
                                PodiumItem(
                                    rang = 1, profil = classement[0], hauteur = 110.dp, couronne = true,
                                    onClick = if (classement[0].userId != socialViewModel.monUserId)
                                        { { onClickProfil?.invoke(classement[0].userId) } } else null
                                )
                                PodiumItem(
                                    rang = 3, profil = classement[2], hauteur = 60.dp,
                                    onClick = if (classement[2].userId != socialViewModel.monUserId)
                                        { { onClickProfil?.invoke(classement[2].userId) } } else null
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // Liste rang 4+
                    itemsIndexed(classement.drop(3)) { index, profil ->
                        LeaderboardRow(
                            rang = index + 4,
                            profil = profil,
                            estMoi = profil.userId == socialViewModel.monUserId,
                            onClick = if (profil.userId != socialViewModel.monUserId)
                                { { onClickProfil?.invoke(profil.userId) } } else null
                        )
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PodiumItem(
    rang: Int,
    profil: ProfilPublic,
    hauteur: Dp,
    couronne: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        if (couronne) {
            Text("👑", fontSize = 20.sp)
        } else {
            Spacer(Modifier.height(28.dp))
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(VioletFit.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emojiAnimal(profil.avatarEspece),
                fontSize = 24.sp
            )
        }
        Text(
            text = profil.nom.split(" ").first(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "%.0f pts".format(profil.scoreHebdo),
            color = MintFit,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(hauteur)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    when (rang) {
                        1    -> AmberFit
                        2    -> Color(0xFF9E9E9E)
                        else -> Color(0xFF8D6E63)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rang",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    rang: Int,
    profil: ProfilPublic,
    estMoi: Boolean,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (estMoi) VioletFit.copy(alpha = 0.08f) else Color.Transparent)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rang",
            color = if (estMoi) VioletFit else TextDim,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(40.dp)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CardBG),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emojiAnimal(profil.avatarEspece), fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = profil.nom,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%.0f pts".format(profil.scoreHebdo),
            color = if (estMoi) VioletFit else MintFit,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

private fun emojiAnimal(espece: String): String = when (espece) {
    "renard"   -> "🦊"
    "pingouin" -> "🐧"
    "panda"    -> "🐼"
    "axolotl"  -> "🦎"
    else       -> "🐾"
}
