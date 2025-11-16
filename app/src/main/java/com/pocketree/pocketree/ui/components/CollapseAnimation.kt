package com.pocketree.pocketree.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

/**
 * Small collapse animation:
 * - Tree shrinks from scale 1f → 0.2f
 * - Rotates by 20 degrees in collapse
 * - Fades out slightly
 * - Moves downward
 */
@Composable
fun CollapseAnimation(
    trigger: Boolean,
    content: @Composable () -> Unit
) {
    var start by remember { mutableStateOf(false) }

    // start animation when "trigger=true"
    LaunchedEffect(trigger) {
        if (trigger) start = true
    }

    val scale by animateFloatAsState(
        targetValue = if (start) 0.2f else 1f,
        animationSpec = tween(500),
        label = ""
    )

    val rotation by animateFloatAsState(
        targetValue = if (start) 20f else 0f,
        animationSpec = tween(500),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (start) 0.4f else 1f,
        animationSpec = tween(500),
        label = ""
    )

    val drop by animateDpAsState(
        targetValue = if (start) 25.dp else 0.dp,
        animationSpec = tween(500),
        label = ""
    )

    Box(
        modifier = Modifier
            .offset(y = drop)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
