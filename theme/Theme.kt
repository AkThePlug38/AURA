package com.Rajath.aura.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Build color schemes using the palette in colors.kt
private val AuraLightColors: ColorScheme = lightColorScheme(
    primary = AuraPrimary,
    onPrimary = Color.White,
    secondary = AuraCyan,
    onSecondary = AuraDark,
    background = AuraLight,
    onBackground = AuraDark,
    surface = Color(0xFFF6FBFF),
    onSurface = AuraDark,
    error = Color(0xFFB00020)
)

private val AuraDarkColors: ColorScheme = darkColorScheme(
    primary = AuraPrimary,
    onPrimary = Color.White,
    secondary = AuraCyan,
    onSecondary = AuraLight,
    background = AuraDark,
    onBackground = AuraLight,
    surface = Color(0xFF0F1724),
    onSurface = AuraLight,
    error = Color(0xFFCF6679)
)

@Composable
fun AURATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // allow Material dynamic colors on Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AuraDarkColors
        else -> AuraLightColors
    }

    // set status bar / navigation bar colors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // make sure we can control system bars
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun AuraGradientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(AuraBgStart, AuraBgEnd)
                )
            )
    ) {
        content()
    }
}