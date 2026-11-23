package com.aswfoods.fieldtrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AswPrimaryRed,
    onPrimary = Color.White,
    primaryContainer = AswSurfaceLight,
    onPrimaryContainer = AswCharcoal,
    secondary = AswCharcoal,
    onSecondary = Color.White,
    tertiary = AswAccentRed,
    background = AswBackground,
    onBackground = AswCharcoal,
    surface = AswSurfaceLight,
    onSurface = AswCharcoal,
    surfaceVariant = AswBorder,
    onSurfaceVariant = AswSecondaryText,
    error = AswErrorRed,
    onError = Color.White
)

@Composable
fun FieldTrackTheme(
    content: @Composable () -> Unit
) {
    // Aplikasi didesain khusus tema terang (Light Theme) sesuai pedoman merek ASW Foods
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

