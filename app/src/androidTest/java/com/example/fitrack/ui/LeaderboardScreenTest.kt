package com.example.fitrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitrack.fakes.FakeSocialRepository
import com.example.fitrack.interface_ui.LeaderboardScreen
import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.SocialViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LeaderboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onglets_global_et_categorie_affiches() {
        val vm = SocialViewModel(FakeSocialRepository())
        composeTestRule.setContent {
            FitrackTheme {
                LeaderboardScreen(socialViewModel = vm, userId = "u1", categorie = "debutant")
            }
        }
        composeTestRule.onNodeWithText("Général").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ma catégorie").assertIsDisplayed()
    }

    @Test
    fun bandeau_ma_position_affiche() {
        val vm = SocialViewModel(FakeSocialRepository())
        composeTestRule.setContent {
            FitrackTheme {
                LeaderboardScreen(socialViewModel = vm, userId = "u1", categorie = "debutant")
            }
        }
        composeTestRule.onNodeWithText("Moi").assertIsDisplayed()
    }

    @Test
    fun titre_classement_affiche() {
        val vm = SocialViewModel(FakeSocialRepository())
        composeTestRule.setContent {
            FitrackTheme {
                LeaderboardScreen(socialViewModel = vm, userId = "u1", categorie = "debutant")
            }
        }
        composeTestRule.onNodeWithText("Classement").assertIsDisplayed()
    }

    @Test
    fun noms_joueurs_affiches_quand_donnees_presentes() {
        val repo = FakeSocialRepository().apply {
            lireClassementResult = Result.success(
                listOf(
                    ProfilPublic(userId = "u1", nom = "Alice", scoreHebdo = 900.0, categorie = "debutant"),
                    ProfilPublic(userId = "u2", nom = "Bob",   scoreHebdo = 700.0, categorie = "debutant"),
                    ProfilPublic(userId = "u3", nom = "Carol", scoreHebdo = 500.0, categorie = "debutant")
                )
            )
        }
        val vm = SocialViewModel(repo)
        composeTestRule.setContent {
            FitrackTheme {
                LeaderboardScreen(socialViewModel = vm, userId = "u1", categorie = "debutant")
            }
        }
        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(hasText("Alice")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
    }
}
