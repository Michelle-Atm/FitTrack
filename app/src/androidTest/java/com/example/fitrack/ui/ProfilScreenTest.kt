package com.example.fitrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitrack.fakes.FakeAuthRepository
import com.example.fitrack.interface_ui.ProfilScreen
import com.example.fitrack.model.User
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildScreen() {
        val repo = FakeAuthRepository().apply {
            utilisateurObserve = User(
                uid = "u1", nom = "TestUser", email = "test@test.com",
                poids = 70.0, taille = 175
            )
        }
        val vm = AuthViewModel(repo)
        composeTestRule.setContent {
            FitrackTheme {
                ProfilScreen(
                    viewModel = vm,
                    navController = rememberNavController()
                )
            }
        }
    }

    @Test
    fun bouton_sauvegarder_affiche() {
        buildScreen()
        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(hasTestTag("profil_save_btn")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("profil_save_btn").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun bouton_deconnexion_affiche() {
        buildScreen()
        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(hasTestTag("profil_logout_btn")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("profil_logout_btn").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun toggle_mode_sombre_affiche() {
        buildScreen()
        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(hasTestTag("profil_dark_mode_switch")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("profil_dark_mode_switch").performScrollTo().assertIsDisplayed()
    }
}
