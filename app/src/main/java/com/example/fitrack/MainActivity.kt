package com.example.fitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fitrack.interface_ui.DarkBG
import com.example.fitrack.interface_ui.LoginScreen
import com.example.fitrack.interface_ui.MintFit
import com.example.fitrack.ui.theme.FitrackTheme
import com.example.fitrack.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitrackTheme {
                val uiState by authViewModel.uiState.collectAsState()

                when (uiState) {
                    is AuthViewModel.AuthUiState.Succes -> {
                        val user = (uiState as AuthViewModel.AuthUiState.Succes).utilisateur
                        // TODO(HomeScreen) : remplacer par NavHost + HomeScreen
                        Box(
                            modifier = Modifier.fillMaxSize().background(DarkBG),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bienvenue ${user.nom.ifBlank { user.email }}",
                                color = MintFit,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> LoginScreen(viewModel = authViewModel)
                }
            }
        }
    }
}
