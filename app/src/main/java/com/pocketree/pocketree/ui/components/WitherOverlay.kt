package com.pocketree.pocketree.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Subtle wither overlay (no red X).
 * Keep it very faint; this is optional and non-invasive.
 */
@Composable
fun WitherOverlay(visible: Boolean, holdMs: Long = 700L, onFinished: (() -> Unit)? = null) {
    var show by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            show = true
            if (holdMs > 0L) {
                kotlinx.coroutines.delay(holdMs)
                onFinished?.invoke()
            }
        } else {
            show = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (show) 0.12f else 0f,
        animationSpec = tween(300),
        label = ""
    )

    if (!show && alpha <= 0f) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3B2B1E).copy(alpha = alpha))
    )
}
