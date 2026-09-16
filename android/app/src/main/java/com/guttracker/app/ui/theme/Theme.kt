package com.guttracker.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AppColors.Accent,
    onPrimary = AppColors.Surface,
    secondary = AppColors.Sage,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    error = AppColors.Danger,
)

@Composable
fun GutTrackerTheme(content: @Composable () -> Unit) {
    // the design is deliberately light-only — a warm cream/terracotta palette rather than
    // a stock light/dark Material pair, so dark mode isn't offered yet.
    MaterialTheme(colorScheme = LightColors, typography = AppTypography, content = content)
}
