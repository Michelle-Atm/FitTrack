package com.example.fitrack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VioletFit,      // Ton violet
    secondary = MintFit,      // Ton vert menthe
    tertiary = AmberFit,      // Ton orange
    background = DarkBG,      // Ton fond noir
    surface = CardBG          // Tes cartes
)

private val LightColorScheme = lightColorScheme(
    primary = VioletFit,
    secondary = MintFit,
    tertiary = AmberFit
)

@Composable
fun FitrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}