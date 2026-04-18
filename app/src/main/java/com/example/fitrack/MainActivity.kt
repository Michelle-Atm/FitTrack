package com.example.fitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.fitrack.interface_ui.LoginScreen // L'import crucial
import com.example.fitrack.ui.theme.FitrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitrackTheme {
                LoginScreen()
            }
        }
    }
}