package com.pocketree.pocketree.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pocketree.pocketree.R
import kotlin.random.Random

enum class TreeStage {
    SEED, SAPLING, YOUNG, FULL
}

@Composable
fun AnimatedTree(
    stage: TreeStage,
    modifier: Modifier = Modifier,
    animate: Boolean = false,
    treeWithered: Boolean = false
) {
    val healthyRes = when (stage) {
        TreeStage.SEED -> R.drawable.ic_tree_seed
        TreeStage.SAPLING -> R.drawable.ic_tree_sprout
        TreeStage.YOUNG -> R.drawable.ic_tree_young
        TreeStage.FULL -> R.drawable.ic_tree_full
    }

    val deadRes = when (stage) {
        TreeStage.SEED -> R.drawable.ic_tree_dead_seed
        TreeStage.SAPLING -> R.drawable.ic_tree_dead_sprout
        TreeStage.YOUNG -> R.drawable.ic_tree_dead_small
        TreeStage.FULL -> R.drawable.ic_tree_dead_full
    }

    val res = if (treeWithered) deadRes else healthyRes

    // FIXED SIZE — NEVER SHRINK OR GROW
    val displaySizeDp = 220.dp

    // If no animation or withered → draw static tree
    if (!animate || treeWithered) {
        Box(
            modifier = modifier.size(displaySizeDp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = res),
                contentDescription = null,
                modifier = Modifier.size(displaySizeDp)
            )
        }
        return
    }

    // Per-tree random animation offset (forest looks natural)
    val phaseOffset = remember { Random.nextInt(0, 900) }

    val transition = rememberInfiniteTransition(label = "treeSway-$phaseOffset")

    // Gentle sway rotation
    val rotation by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600 + phaseOffset, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Vertical bob
    val bobPx by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600 + phaseOffset, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Convert to dp
    val density = LocalDensity.current
    val bobDp = with(density) { (bobPx / density.density).dp }

    // FINAL animated tree
    Box(
        modifier = modifier
            .size(displaySizeDp)
            .rotate(rotation)
            .offset(y = bobDp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = res),
            contentDescription = null,
            modifier = Modifier.size(displaySizeDp)
        )
    }
}
