package com.pocketree.pocketree.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color   // REQUIRED import

private val LightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = OnPrimary,
    surface = NeutralSurface,
    background = SoftMint,
    onSurface = OnSurfaceDark,
    secondary = LightGreen,
    tertiary = EarthBrown
)

private val DarkColors = darkColorScheme(
    primary = ForestGreen,
    onPrimary = OnPrimary,
    surface = Color(0xFF0B1F12),
    background = Color(0xFF07120A),
    onSurface = Color(0xFFDDEFE0),
    secondary = LightGreen
)

@Composable
fun PocketreeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (!darkTheme) LightColors else DarkColors

    MaterialTheme(
        colorScheme = colors,
        typography = PocketTypography,
        shapes = PocketShapes,
        content = content
    )
}
