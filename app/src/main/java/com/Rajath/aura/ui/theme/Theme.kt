package com.Rajath.aura.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    secondary = BrandCyan,
    onSecondary = BrandDark,
    tertiary = BrandPale,
    onTertiary = BrandDark,
    background = SurfaceDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceElevated,
    outline = AccentMuted,
    error = Color(0xFFEF4444)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    secondary = BrandCyan,
    onSecondary = BrandDark,
    tertiary = BrandPale,
    onTertiary = BrandDark,
    background = BrandPale,
    onBackground = BrandDark,
    surface = Color.White,
    onSurface = BrandDark,
    surfaceVariant = BrandPale,
    outline = AccentLight,
    error = Color(0xFFB00020)
)

@Composable
fun AURATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // default to false while you iterate — set true later if you want dynamic Material You colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}