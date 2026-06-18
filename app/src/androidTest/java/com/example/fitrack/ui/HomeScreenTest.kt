package com.example.fitrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitrack.interface_ui.HomeScreen
import com.example.fitrack.model.User
import com.example.fitrack.ui.theme.FitrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun salutation_affichee_avec_nom_utilisateur() {
        composeTestRule.setContent {
            FitrackTheme {
                HomeScreen(
                    user = User(uid = "u1", nom = "Alice", email = "alice@test.com"),
                    navController = rememberNavController()
                )
            }
        }
        composeTestRule.onNodeWithTag("home_greeting").assertIsDisplayed()
    }

    @Test
    fun chargement_affiche_si_user_null() {
        composeTestRule.setContent {
            FitrackTheme {
                HomeScreen(user = null, navController = rememberNavController())
            }
        }
        composeTestRule.onNodeWithTag("home_greeting").assertDoesNotExist()
    }

    @Test
    fun bouton_nutrition_affiche() {
        composeTestRule.setContent {
            FitrackTheme {
                HomeScreen(
                    user = User(uid = "u1", nom = "Alice", email = "a@a.com"),
                    navController = rememberNavController()
                )
            }
        }
        composeTestRule.onNodeWithTag("home_btn_nutrition").assertIsDisplayed()
    }

    @Test
    fun bouton_objectifs_affiche() {
        composeTestRule.setContent {
            FitrackTheme {
                HomeScreen(
                    user = User(uid = "u1", nom = "Alice", email = "a@a.com"),
                    navController = rememberNavController()
                )
            }
        }
        composeTestRule.onNodeWithTag("home_btn_objectifs").assertIsDisplayed()
    }
}
