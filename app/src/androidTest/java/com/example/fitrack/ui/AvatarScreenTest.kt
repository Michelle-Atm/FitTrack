package com.example.fitrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitrack.fakes.FakeAuthRepository
import com.example.fitrack.interface_ui.AvatarScreen
import com.example.fitrack.model.User
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.AvatarViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvatarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildScreen(): AvatarViewModel {
        val repo = FakeAuthRepository().apply {
            utilisateurObserve = User(uid = "u1", nom = "Test", email = "t@t.com")
        }
        val vm = AvatarViewModel(repo)
        composeTestRule.setContent {
            FitrackTheme {
                AvatarScreen(avatarViewModel = vm, userId = "u1")
            }
        }
        return vm
    }

    @Test
    fun section_historique_affichee() {
        buildScreen()
        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(hasText("HISTORIQUE")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("HISTORIQUE").assertIsDisplayed()
    }

    @Test
    fun message_historique_vide_affiche() {
        buildScreen()
        composeTestRule.waitUntil(3_000) {
            composeTestRule.onAllNodes(hasTestTag("avatar_historique_vide")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("avatar_historique_vide").performScrollTo().assertIsDisplayed()
    }
}
