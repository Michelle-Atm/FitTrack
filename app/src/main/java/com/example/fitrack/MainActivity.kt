package com.example.fitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.fitrack.navigation.FitTrackNavGraph
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.AuthViewModel
import com.example.fitrack.viewmodel.NutritionViewModel
import com.example.fitrack.viewmodel.ObjectifViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val nutritionViewModel: NutritionViewModel by viewModels()
    private val objectifViewModel: ObjectifViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitrackTheme {
                FitTrackNavGraph(
                    authViewModel = authViewModel,
                    nutritionViewModel = nutritionViewModel,
                    objectifViewModel = objectifViewModel
                )
            }
        }
    }
}
