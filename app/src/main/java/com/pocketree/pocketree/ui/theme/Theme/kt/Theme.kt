package com.pocketree.pocketree.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    surface = ForestSurface,
    background = ForestBackground,
    onSurface = ForestOnSurface,
    secondary = ForestSecondary,
    tertiary = ForestTertiary
)

private val DarkColors = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    surface = ForestSurface,
    background = ForestBackground,
    onSurface = ForestOnSurface,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    surfaceVariant = ForestSurfaceVariant
)

@Composable
fun PocketreeTheme(
    darkTheme: Boolean = true, // default to dark / forest look
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = PocketTypography,
        shapes = PocketShapes,  // keep your existing shapes
        content = content
    )
}
