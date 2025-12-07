package com.pocketree.pocketree.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.alpha

@Composable
fun WitherOverlay(
    visible: Boolean,
    holdMs: Long = 700L,
    onFinished: (() -> Unit)? = null
) {
    var show by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            show = true
            delay(holdMs)
            show = false
            delay(380)
            onFinished?.invoke()
        } else {
            show = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = tween(380),
        label = ""
    )

    if (!visible && alpha <= 0f) return

    // Draw a subtle translucent warm overlay + thin X lines in muted red
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3B2B1E).copy(alpha = 0.06f * alpha))
            .alpha(alpha)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.02f // thinner
            drawLine(
                color = Color(0xFFB71C1C).copy(alpha = 0.75f * alpha),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFB71C1C).copy(alpha = 0.75f * alpha),
                start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                end = androidx.compose.ui.geometry.Offset(0f, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
