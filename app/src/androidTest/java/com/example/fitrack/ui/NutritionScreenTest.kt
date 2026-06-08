package com.example.fitrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitrack.fakes.FakeNutritionRepository
import com.example.fitrack.fakes.FakeObjectifRepository
import com.example.fitrack.interface_ui.NutritionScreen
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.NutritionViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NutritionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildScreen() {
        val vm = NutritionViewModel(FakeNutritionRepository(), FakeObjectifRepository())
        composeTestRule.setContent {
            FitrackTheme {
                NutritionScreen(
                    viewModel = vm,
                    userId = "u1",
                    onAjouterRepas = {},
                    onHistorique = {}
                )
            }
        }
    }

    @Test
    fun fab_ajout_repas_affiche() {
        buildScreen()
        composeTestRule.onNodeWithTag("nutrition_fab").assertIsDisplayed()
    }

    @Test
    fun bouton_historique_affiche() {
        buildScreen()
        composeTestRule.onNodeWithTag("nutrition_historique_btn").assertIsDisplayed()
    }
}
