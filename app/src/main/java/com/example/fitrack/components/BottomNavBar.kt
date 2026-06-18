package com.example.fitrack.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fitrack.navigation.ROUTE_AVATAR
import com.example.fitrack.navigation.ROUTE_HOME
import com.example.fitrack.navigation.ROUTE_LEADERBOARD
import com.example.fitrack.navigation.ROUTE_NUTRITION
import com.example.fitrack.navigation.ROUTE_OBJECTIFS
import com.example.fitrack.navigation.ROUTE_PROFIL
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextFaint

sealed class NavItem(val route: String, val label: String, val icon: ImageVector) {
    object Accueil      : NavItem(ROUTE_HOME,        "Accueil",    Icons.Default.Home)
    object Nutrition    : NavItem(ROUTE_NUTRITION,   "Nutrition",  Icons.Default.Restaurant)
    object Objectifs    : NavItem(ROUTE_OBJECTIFS,   "Objectifs",  Icons.Default.TrackChanges)
    object Avatar       : NavItem(ROUTE_AVATAR,      "Avatar",     Icons.Default.Pets)
    object Classement   : NavItem(ROUTE_LEADERBOARD, "Classement", Icons.Default.EmojiEvents)
    object Profil       : NavItem(ROUTE_PROFIL,      "Profil",     Icons.Default.Person)
}

// 5 onglets : Accueil · Nutrition · Objectifs · Avatar · Classement
// Profil accessible via HomeScreen pour ne pas surcharger la barre
private val navItems = listOf(
    NavItem.Accueil,
    NavItem.Nutrition,
    NavItem.Objectifs,
    NavItem.Avatar,
    NavItem.Classement
)

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    NavigationBar(containerColor = DarkBG, tonalElevation = 0.dp) {
        navItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(ROUTE_HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = {
                    Text(text = item.label, fontSize = 10.sp)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MintFit,
                    selectedTextColor = MintFit,
                    unselectedIconColor = TextFaint,
                    unselectedTextColor = TextFaint,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
