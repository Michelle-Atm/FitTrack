package com.example.fitrack.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitrack.components.BottomNavBar
import com.example.fitrack.interface_ui.HistoriqueNutritionScreen
import com.example.fitrack.interface_ui.HomeScreen
import com.example.fitrack.interface_ui.InscriptionScreen
import com.example.fitrack.interface_ui.LoginScreen
import com.example.fitrack.interface_ui.NutritionScreen
import com.example.fitrack.interface_ui.ObjectifsScreen
import com.example.fitrack.interface_ui.ProfilScreen
import com.example.fitrack.interface_ui.SaisieRepasScreen
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.viewmodel.AuthViewModel
import com.example.fitrack.viewmodel.NutritionViewModel
import com.example.fitrack.viewmodel.ObjectifViewModel

const val ROUTE_LOGIN       = "login"
const val ROUTE_INSCRIPTION = "inscription"
const val ROUTE_HOME        = "home"
const val ROUTE_PROFIL      = "profil"
const val ROUTE_NUTRITION   = "nutrition"
const val ROUTE_SAISIE_REPAS = "saisie_repas"
const val ROUTE_OBJECTIFS   = "objectifs"
const val ROUTE_HISTORIQUE  = "historique_nutrition"

private val ROUTES_WITH_NAV = setOf(ROUTE_HOME, ROUTE_NUTRITION, ROUTE_OBJECTIFS, ROUTE_PROFIL)

@Composable
fun FitTrackNavGraph(
    authViewModel: AuthViewModel,
    nutritionViewModel: NutritionViewModel,
    objectifViewModel: ObjectifViewModel
) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val utilisateurActuel by authViewModel.utilisateurActuel.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthViewModel.AuthUiState.Succes -> navController.navigate(ROUTE_HOME) {
                popUpTo(ROUTE_LOGIN) { inclusive = true }
            }
            is AuthViewModel.AuthUiState.Deconnecte -> navController.navigate(ROUTE_LOGIN) {
                popUpTo(0) { inclusive = true }
            }
            else -> {}
        }
    }

    LaunchedEffect(utilisateurActuel) {
        if (utilisateurActuel != null && uiState !is AuthViewModel.AuthUiState.Succes) {
            val current = navController.currentDestination?.route
            if (current == ROUTE_LOGIN || current == ROUTE_INSCRIPTION || current == null) {
                navController.navigate(ROUTE_HOME) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    val user = (uiState as? AuthViewModel.AuthUiState.Succes)?.utilisateur ?: utilisateurActuel
    val userId = user?.uid ?: ""

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route?.substringBefore("?")

    Scaffold(
        containerColor = DarkBG,
        bottomBar = {
            if (currentRoute in ROUTES_WITH_NAV) {
                BottomNavBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_LOGIN,
            modifier = Modifier.padding(padding)
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigerVersInscription = { navController.navigate(ROUTE_INSCRIPTION) }
                )
            }
            composable(ROUTE_INSCRIPTION) {
                InscriptionScreen(
                    viewModel = authViewModel,
                    onRetourLogin = { navController.popBackStack() }
                )
            }
            composable(ROUTE_HOME) {
                HomeScreen(user = user, navController = navController)
            }
            composable(ROUTE_PROFIL) {
                ProfilScreen(viewModel = authViewModel, navController = navController)
            }
            composable(
                route = "$ROUTE_NUTRITION?date={date}",
                arguments = listOf(navArgument("date") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStack ->
                val dateArg = backStack.arguments?.getLong("date") ?: 0L
                NutritionScreen(
                    viewModel = nutritionViewModel,
                    userId = userId,
                    dateOverride = if (dateArg == 0L) null else dateArg,
                    onAjouterRepas = { navController.navigate(ROUTE_SAISIE_REPAS) },
                    onHistorique = { navController.navigate(ROUTE_HISTORIQUE) }
                )
            }
            composable(ROUTE_SAISIE_REPAS) {
                SaisieRepasScreen(
                    viewModel = nutritionViewModel,
                    userId = userId,
                    onRetour = { navController.popBackStack() },
                    allergiesUtilisateur = user?.allergies ?: emptyList()
                )
            }
            composable(ROUTE_OBJECTIFS) {
                ObjectifsScreen(
                    viewModel = objectifViewModel,
                    userId = userId
                )
            }
            composable(ROUTE_HISTORIQUE) {
                HistoriqueNutritionScreen(
                    viewModel = nutritionViewModel,
                    userId = userId,
                    onRetour = { navController.popBackStack() },
                    onOuvrirJour = { date ->
                        navController.navigate("$ROUTE_NUTRITION?date=$date")
                    }
                )
            }
        }
    }
}
