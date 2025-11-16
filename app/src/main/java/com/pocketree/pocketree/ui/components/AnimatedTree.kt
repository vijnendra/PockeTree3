package com.pocketree.pocketree.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pocketree.pocketree.R

enum class TreeStage(val scale: Float, val alpha: Float) {
    SEED(0.25f, 0.40f),
    SAPLING(0.55f, 0.70f),
    YOUNG(0.80f, 0.90f),
    FULL(1.0f, 1.0f)
}

@Composable
fun AnimatedTree(
    stage: TreeStage,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    // Growth
    val scale by animateFloatAsState(
        targetValue = stage.scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = stage.alpha,
        animationSpec = tween(700),
        label = ""
    )

    // Wind sway
    val wind = rememberInfiniteTransition()
    val sway by wind.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // ------------------------
    // 🎨 SELECT ONLY RED TREE
    // ------------------------
    val sprite = when (stage) {
        TreeStage.SEED -> R.drawable.ic_tree_red_seed
        TreeStage.SAPLING -> R.drawable.ic_tree_red_sapling
        TreeStage.YOUNG -> R.drawable.ic_tree_red_small
        TreeStage.FULL -> R.drawable.ic_tree_red_full
    }

    Box(
        modifier = modifier
            .size(200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = if (animate) sway else 0f
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(sprite),
            contentDescription = "Red Fruit Tree",
            modifier = Modifier.fillMaxSize()
        )
    }
}
