package com.example.fitrack.interface_ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.DarkText
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit

@Composable
fun HomeScreen(user: User?, navController: NavController) {
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bonjour, ${user.nom.ifBlank { user.email }}",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.semantics { testTag = "home_greeting" }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Score total",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim
        )
        Text(
            text = "${user.xp} pts",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            ),
            color = VioletFit
        )
        Spacer(modifier = Modifier.height(40.dp))

        // Raccourcis principaux
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NavShortcut(label = "Nutrition", color = MintFit, tag = "home_btn_nutrition") {
                navController.navigate(ROUTE_NUTRITION)
            }
            NavShortcut(label = "Objectifs", color = VioletFit, tag = "home_btn_objectifs") {
                navController.navigate(ROUTE_OBJECTIFS)
            }
            NavShortcut(label = "Mon Profil", color = CardBG, tag = "home_btn_profil") {
                navController.navigate(ROUTE_PROFIL)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Raccourcis Sprint 2
        Text(
            text = "ACTIVITÉ",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
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
            RaccourciCard(
                label = "Classement",
                icone = Icons.Default.EmojiEvents,
                accent = AmberFit,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(ROUTE_LEADERBOARD) }
        }
    }
}

@Composable
private fun NavShortcut(label: String, color: Color, tag: String = "", onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (tag.isNotEmpty()) Modifier.semantics { testTag = tag } else Modifier),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (color == CardBG) Color.White else DarkText
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
        modifier = modifier.height(72.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}
