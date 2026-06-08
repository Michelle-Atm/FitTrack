package com.example.fitrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitrack.fakes.FakeNutritionRepository
import com.example.fitrack.fakes.FakeObjectifRepository
import com.example.fitrack.interface_ui.SaisieRepasScreen
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.NutritionViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaisieRepasScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildScreen() {
        val vm = NutritionViewModel(FakeNutritionRepository(), FakeObjectifRepository())
        composeTestRule.setContent {
            FitrackTheme {
                SaisieRepasScreen(
                    viewModel = vm,
                    userId = "u1",
                    onRetour = {},
                    allergiesUtilisateur = emptyList()
                )
            }
        }
    }

    @Test
    fun bouton_retour_affiche() {
        buildScreen()
        composeTestRule.onNodeWithTag("saisie_back_btn").assertIsDisplayed()
    }

    @Test
    fun champ_recherche_affiche() {
        buildScreen()
        composeTestRule.onNodeWithTag("saisie_search_field").assertIsDisplayed()
    }

    @Test
    fun champ_recherche_accepte_saisie() {
        buildScreen()
        composeTestRule.onNodeWithTag("saisie_search_field").performTextInput("pomme")
        composeTestRule.onNodeWithTag("saisie_search_field").assertIsDisplayed()
    }

    @Test
    fun bouton_ajouter_affiche() {
        buildScreen()
        composeTestRule.onNodeWithTag("saisie_add_btn").assertIsDisplayed()
    }
}
