package com.example.fitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.example.fitrack.navigation.FitTrackNavGraph
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.AuthViewModel
import com.example.fitrack.viewmodel.NutritionViewModel
import com.example.fitrack.viewmodel.ObjectifViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val nutritionViewModel: NutritionViewModel by viewModels()
    private val objectifViewModel: ObjectifViewModel by viewModels()

    private var isDarkMode by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val prefs = getSharedPreferences("fitrack_prefs", MODE_PRIVATE)
        isDarkMode = prefs.getBoolean("dark_mode", true)

        setContent {
            FitrackTheme(darkTheme = isDarkMode) {
                FitTrackNavGraph(
                    authViewModel = authViewModel,
                    nutritionViewModel = nutritionViewModel,
                    objectifViewModel = objectifViewModel,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = {
                        isDarkMode = !isDarkMode
                        prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
                    }
                )
            }
        }
    }
}
