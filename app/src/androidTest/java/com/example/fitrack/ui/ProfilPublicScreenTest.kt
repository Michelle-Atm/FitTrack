package com.example.fitrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitrack.fakes.FakeSocialRepository
import com.example.fitrack.interface_ui.ProfilPublicScreen
import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.SocialViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilPublicScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bouton_retour_affiche() {
        val vm = SocialViewModel(FakeSocialRepository())
        composeTestRule.setContent {
            FitrackTheme {
                ProfilPublicScreen(socialViewModel = vm, userId = "", onRetour = {})
            }
        }
        composeTestRule.onNodeWithTag("profil_public_back").assertIsDisplayed()
    }

    @Test
    fun chargement_affiche_si_profil_non_charge() {
        val vm = SocialViewModel(FakeSocialRepository())
        composeTestRule.setContent {
            FitrackTheme {
                ProfilPublicScreen(socialViewModel = vm, userId = "", onRetour = {})
            }
        }
        composeTestRule.onNodeWithTag("profil_public_loading").assertIsDisplayed()
    }

    @Test
    fun profil_affiche_quand_charge() {
        val repo = FakeSocialRepository().apply {
            lireClassementResult = Result.success(
                listOf(ProfilPublic(userId = "u42", nom = "Zara", scoreHebdo = 800.0))
            )
        }
        val vm = SocialViewModel(repo)
        composeTestRule.setContent {
            FitrackTheme {
                ProfilPublicScreen(socialViewModel = vm, userId = "u42", onRetour = {})
            }
        }
        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(hasText("Zara")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Zara").assertIsDisplayed()
    }
}
