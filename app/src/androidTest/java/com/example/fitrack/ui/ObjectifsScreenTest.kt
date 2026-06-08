package com.example.fitrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitrack.components.CelebrationOverlay
import com.example.fitrack.fakes.FakeObjectifRepository
import com.example.fitrack.interface_ui.ObjectifsScreen
import com.example.fitrack.model.User
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.ObjectifViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ObjectifsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildScreen(repo: FakeObjectifRepository = FakeObjectifRepository()): ObjectifViewModel {
        val vm = ObjectifViewModel(repo)
        composeTestRule.setContent {
            FitrackTheme {
                ObjectifsScreen(
                    viewModel = vm,
                    userId = "u1",
                    user = User(uid = "u1", nom = "Test", email = "t@t.com")
                )
            }
        }
        return vm
    }

    @Test
    fun bouton_logger_seance_affiche() {
        buildScreen()
        composeTestRule.onNodeWithTag("objectifs_logger_btn").assertIsDisplayed()
    }

    @Test
    fun section_side_quest_visible() {
        buildScreen()
        composeTestRule.onNodeWithText("SIDE QUEST").assertIsDisplayed()
    }

    @Test
    fun score_du_jour_visible() {
        buildScreen()
        composeTestRule.onNodeWithText("Score du jour").assertIsDisplayed()
    }

    @Test
    fun celebration_overlay_visible_quand_actif() {
        composeTestRule.setContent {
            FitrackTheme {
                CelebrationOverlay(
                    visible = true,
                    message = "Objectif atteint !",
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Objectif atteint !").assertIsDisplayed()
    }

    @Test
    fun celebration_overlay_masque_quand_inactif() {
        composeTestRule.setContent {
            FitrackTheme {
                CelebrationOverlay(
                    visible = false,
                    message = "Objectif atteint !",
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Objectif atteint !").assertDoesNotExist()
    }
}
