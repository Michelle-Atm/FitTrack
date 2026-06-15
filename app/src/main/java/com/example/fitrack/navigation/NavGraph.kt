package com.example.fitrack.navigation

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitrack.components.BottomNavBar
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitrack.interface_ui.AdminPanelScreen
import com.example.fitrack.interface_ui.AvatarScreen
import com.example.fitrack.interface_ui.BarcodeScannerScreen
import com.example.fitrack.interface_ui.ChoixEspeceScreen
import com.example.fitrack.interface_ui.GpsTrajetScreen
import com.example.fitrack.interface_ui.HistoriqueNutritionScreen
import com.example.fitrack.interface_ui.HomeScreen
import com.example.fitrack.interface_ui.InscriptionScreen
import com.example.fitrack.interface_ui.LeaderboardScreen
import com.example.fitrack.interface_ui.LoginScreen
import com.example.fitrack.interface_ui.NutritionScreen
import com.example.fitrack.interface_ui.ObjectifsScreen
import com.example.fitrack.interface_ui.PodometreScreen
import com.example.fitrack.interface_ui.ProfilPublicScreen
import com.example.fitrack.interface_ui.ProfilScreen
import com.example.fitrack.interface_ui.SaisieRepasScreen
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.viewmodel.AdminViewModel
import com.example.fitrack.viewmodel.AuthViewModel
import com.example.fitrack.viewmodel.AvatarViewModel
import com.example.fitrack.viewmodel.GpsViewModel
import com.example.fitrack.viewmodel.NutritionViewModel
import com.example.fitrack.viewmodel.ObjectifViewModel
import com.example.fitrack.viewmodel.SensorViewModel
import com.example.fitrack.viewmodel.SocialViewModel

const val ROUTE_LOGIN          = "login"
const val ROUTE_INSCRIPTION    = "inscription"
const val ROUTE_HOME           = "home"
const val ROUTE_PROFIL         = "profil"
const val ROUTE_NUTRITION      = "nutrition"
const val ROUTE_PROFIL_PUBLIC  = "profil_public"
const val ROUTE_SAISIE_REPAS = "saisie_repas"
const val ROUTE_OBJECTIFS    = "objectifs"
const val ROUTE_HISTORIQUE   = "historique_nutrition"
const val ROUTE_PODOMETRE    = "podometre"
const val ROUTE_GPS          = "gps_trajet"
const val ROUTE_AVATAR        = "avatar"
const val ROUTE_CHOIX_ESPECE  = "choix_espece"
const val ROUTE_LEADERBOARD      = "leaderboard"
const val ROUTE_BARCODE_SCANNER  = "barcode_scanner"
const val ROUTE_ADMIN            = "admin_panel"

private val ROUTES_WITH_NAV = setOf(
    ROUTE_HOME, ROUTE_NUTRITION, ROUTE_OBJECTIFS, ROUTE_PROFIL, ROUTE_AVATAR, ROUTE_LEADERBOARD
)

@Composable
fun FitTrackNavGraph(
    authViewModel: AuthViewModel,
    nutritionViewModel: NutritionViewModel,
    objectifViewModel: ObjectifViewModel,
    isDarkMode: Boolean = true,
    onToggleDarkMode: () -> Unit = {}
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

    val socialRepository = remember { com.example.fitrack.repository.firestore.FirestoreSocialRepository() }
    LaunchedEffect(user) {
        val currUser = user
        if (currUser != null && currUser.uid.isNotEmpty()) {
            socialRepository.mettreAJourProfil(
                com.example.fitrack.model.ProfilPublic(
                    userId = currUser.uid,
                    nom = currUser.nom.ifBlank { currUser.email.substringBefore("@") },
                    avatarEspece = currUser.avatarEspece.ifBlank { "renard" },
                    scoreHebdo = currUser.xp.toDouble(),
                    categorie = currUser.experience,
                    xpTotal = currUser.xp,
                    niveauActuel = currUser.niveau
                )
            )
        }
    }

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
                HomeScreen(user = user, navController = navController, objectifViewModel = objectifViewModel)
            }
            composable(ROUTE_PROFIL) {
                ProfilScreen(
                    viewModel = authViewModel,
                    navController = navController,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onNavigerVersAdmin = { navController.navigate(ROUTE_ADMIN) }
                )
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
                    allergiesUtilisateur = user?.allergies ?: emptyList(),
                    onScanBarcode = { navController.navigate(ROUTE_BARCODE_SCANNER) }
                )
            }
            composable(ROUTE_BARCODE_SCANNER) {
                BarcodeScannerScreen(
                    onBarcodeDetected = { code ->
                        nutritionViewModel.rechercherParCodeBarres(code)
                        navController.popBackStack()
                    },
                    onRetour = { navController.popBackStack() }
                )
            }
            composable(ROUTE_OBJECTIFS) {
                ObjectifsScreen(
                    viewModel = objectifViewModel,
                    userId = userId,
                    user = user
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
            composable(ROUTE_PODOMETRE) {
                val application = LocalContext.current.applicationContext as Application
                PodometreScreen(
                    sensorViewModel = viewModel(factory = SensorViewModel.factory(application)),
                    objectifViewModel = objectifViewModel
                )
            }
            composable(ROUTE_GPS) {
                val application = LocalContext.current.applicationContext as Application
                GpsTrajetScreen(
                    gpsViewModel = viewModel(factory = GpsViewModel.factory(application)),
                    userId = userId
                )
            }
            composable(ROUTE_AVATAR) {
                AvatarScreen(
                    avatarViewModel = viewModel(factory = AvatarViewModel.Factory),
                    userId = userId,
                    onChangerEspece = { navController.navigate(ROUTE_CHOIX_ESPECE) }
                )
            }
            composable(ROUTE_CHOIX_ESPECE) {
                ChoixEspeceScreen(
                    authViewModel = authViewModel,
                    onConfirme = { navController.popBackStack() }
                )
            }
            composable(ROUTE_LEADERBOARD) {
                LeaderboardScreen(
                    socialViewModel = viewModel(factory = SocialViewModel.Factory),
                    userId = userId,
                    categorie = user?.experience ?: "debutant",
                    onClickProfil = { targetUserId ->
                        navController.navigate("$ROUTE_PROFIL_PUBLIC/$targetUserId")
                    }
                )
            }
            composable(
                route = "$ROUTE_PROFIL_PUBLIC/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStack ->
                val targetUserId = backStack.arguments?.getString("userId") ?: ""
                ProfilPublicScreen(
                    socialViewModel = viewModel(factory = SocialViewModel.Factory),
                    userId = targetUserId,
                    onRetour = { navController.popBackStack() }
                )
            }
            composable(ROUTE_ADMIN) {
                // Route guard — non-admins are ejected immediately
                if (user?.isAdmin != true) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    return@composable
                }
                AdminPanelScreen(
                    adminViewModel = viewModel(factory = AdminViewModel.Factory),
                    onRetour = { navController.popBackStack() }
                )
            }
        }
    }
}
