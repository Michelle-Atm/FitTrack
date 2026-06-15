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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.CardBG2
import com.example.fitrack.ui.theme.CoralFit
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.utils.emojiAnimal
import com.example.fitrack.viewmodel.SocialViewModel
import java.util.Calendar

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
    val monRangCategorie by socialViewModel.monRangCategorie.collectAsStateWithLifecycle()
    val monScoreHebdo by socialViewModel.monScoreHebdo.collectAsStateWithLifecycle()
    val classementGlobal by socialViewModel.classementGlobal.collectAsStateWithLifecycle()
    val classementCategorie by socialViewModel.classementCategorie.collectAsStateWithLifecycle()

    var tabSelectionne by remember { mutableIntStateOf(0) }
    val classement = if (tabSelectionne == 0) classementGlobal else classementCategorie
    val rangAffiche = if (tabSelectionne == 0) monRang else monRangCategorie

    val semaineCourante = remember {
        Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Classement",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = "Semaine $semaineCourante",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = tabSelectionne,
            containerColor = CardBG,
            contentColor = MintFit,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(8.dp)),
            indicator = { tabPositions ->
                if (tabSelectionne < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabSelectionne]),
                        color = VioletFit
                    )
                }
            }
        ) {
            Tab(
                selected = tabSelectionne == 0,
                onClick = { tabSelectionne = 0 },
                text = { Text("Mondial", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (tabSelectionne == 0) Color.White else TextDim) }
            )
            Tab(
                selected = tabSelectionne == 1,
                onClick = { tabSelectionne = 1 },
                text = { Text("Ma Ligue", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (tabSelectionne == 1) Color.White else TextDim) }
            )
        }

        // Gold League Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AmberFit.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Ligue Or — ${if (tabSelectionne == 0) "Tous" else maCategorie.replaceFirstChar { it.uppercase() }}",
                    color = AmberFit,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sticky User Rank Highlight
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = VioletFit.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, VioletFit.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "#$rangAffiche",
                    color = VioletFit,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.width(48.dp)
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
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Podium top 3
                    if (classement.size >= 3) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PodiumItem(
                                    rang = 2,
                                    profil = classement[1],
                                    hauteur = 75.dp,
                                    onClick = if (classement[1].userId != socialViewModel.monUserId) {
                                        { onClickProfil?.invoke(classement[1].userId) }
                                    } else null
                                )
                                PodiumItem(
                                    rang = 1,
                                    profil = classement[0],
                                    hauteur = 105.dp,
                                    couronne = true,
                                    onClick = if (classement[0].userId != socialViewModel.monUserId) {
                                        { onClickProfil?.invoke(classement[0].userId) }
                                    } else null
                                )
                                PodiumItem(
                                    rang = 3,
                                    profil = classement[2],
                                    hauteur = 55.dp,
                                    onClick = if (classement[2].userId != socialViewModel.monUserId) {
                                        { onClickProfil?.invoke(classement[2].userId) }
                                    } else null
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // List 4+
                    itemsIndexed(classement.drop(3)) { index, profil ->
                        LeaderboardRow(
                            rang = index + 4,
                            profil = profil,
                            estMoi = profil.userId == socialViewModel.monUserId,
                            onClick = if (profil.userId != socialViewModel.monUserId) {
                                { onClickProfil?.invoke(profil.userId) }
                            } else null
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
    val accentColor = when (rang) {
        1    -> AmberFit
        2    -> Color(0xFFB4B2A9)
        else -> CoralFit
    }
    val avatarSize = if (rang == 1) 54.dp else 46.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        if (couronne) {
            Text("👑", fontSize = 20.sp, modifier = Modifier.padding(bottom = 2.dp))
        } else {
            Spacer(Modifier.height(26.dp))
        }
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(CardBG)
                .border(2.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emojiAnimal(profil.avatarEspece),
                fontSize = if (rang == 1) 28.sp else 22.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = profil.nom.split(" ").first(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "%.0f pts".format(profil.scoreHebdo),
            color = accentColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(hauteur)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.25f), accentColor.copy(alpha = 0.05f))
                    )
                )
                .border(
                    BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                    RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rang",
                color = accentColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (estMoi) VioletFit.copy(alpha = 0.08f) else CardBG
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (estMoi) VioletFit.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rang",
                color = if (estMoi) VioletFit else TextDim,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.width(36.dp)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CardBG2)
                    .border(1.dp, if (estMoi) VioletFit else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emojiAnimal(profil.avatarEspece), fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = profil.nom,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (estMoi) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(VioletFit)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Moi",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = "%.0f pts".format(profil.scoreHebdo),
                color = if (estMoi) VioletFit else MintFit,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}


