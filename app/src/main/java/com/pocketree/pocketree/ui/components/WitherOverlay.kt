package com.pocketree.pocketree.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Draws a red X overlay with a fade-in animation.
 * Used when the tree is withered.
 */
@Composable
fun WitherOverlay(visible: Boolean) {

    var show by remember { mutableStateOf(false) }

    // Trigger fade-in when visible becomes true
    LaunchedEffect(visible) {
        if (visible) show = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = tween(600),
        label = ""
    )

    if (!visible) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = size.minDimension * 0.08f

        // Draw Red X
        drawLine(
            color = Color(0xFFFF0000).copy(alpha = alpha),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFFFF0000).copy(alpha = alpha),
            start = androidx.compose.ui.geometry.Offset(size.width, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
