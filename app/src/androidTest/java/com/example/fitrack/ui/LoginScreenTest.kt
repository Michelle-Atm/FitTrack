package com.example.fitrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitrack.fakes.FakeAuthRepository
import com.example.fitrack.interface_ui.LoginScreen
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildScreen(repo: FakeAuthRepository = FakeAuthRepository()) {
        val vm = AuthViewModel(repo)
        composeTestRule.setContent {
            FitrackTheme {
                LoginScreen(viewModel = vm, onNavigerVersInscription = {})
            }
        }
    }

    @Test
    fun champ_email_affiche() {
        buildScreen()
        composeTestRule.onNodeWithTag("login_email").assertIsDisplayed()
    }

    @Test
    fun champ_password_affiche() {
        buildScreen()
        composeTestRule.onNodeWithTag("login_password").assertIsDisplayed()
    }

    @Test
    fun bouton_connexion_affiche() {
        buildScreen()
        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
    }

    @Test
    fun erreur_affichee_si_champs_vides() {
        buildScreen()
        composeTestRule.onNodeWithTag("login_button").performClick()
        composeTestRule.onNodeWithTag("login_error").assertIsDisplayed()
    }

    @Test
    fun saisie_email_acceptee() {
        buildScreen()
        composeTestRule.onNodeWithTag("login_email").performTextInput("user@test.com")
        composeTestRule.onNodeWithTag("login_email").assertIsDisplayed()
    }
}
