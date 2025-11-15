package com.pocketree.pocketree.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pocketree.pocketree.R

enum class TreeStage(val scale: Float, val alpha: Float) {
    SEED(0.15f, 0.30f),
    SAPLING(0.45f, 0.60f),
    YOUNG(0.70f, 0.85f),
    FULL(1.0f, 1.0f)
}

@Composable
fun AnimatedTree(
    stage: TreeStage = TreeStage.SEED,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val animatedScale by animateFloatAsState(
        targetValue = stage.scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = stage.alpha,
        animationSpec = tween(durationMillis = 800),
        label = ""
    )

    val wind = rememberInfiniteTransition()
    val sway by wind.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        modifier = modifier
            .size(180.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
                rotationZ = if (animate) sway else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_tree),
            contentDescription = "Animated Tree",
            modifier = Modifier.fillMaxSize()
        )
    }
}
