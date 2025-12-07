package com.pocketree.pocketree.ui.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pocketree.pocketree.R

enum class TreeStage {
    SEED, SAPLING, YOUNG, FULL
}

/**
 * AnimatedTree (robust):
 * - uses BitmapFactory to query intrinsic px size synchronously
 * - computes a stable scaleUpFactor with explicit Density conversion
 * - avoids relying on painter.intrinsicSize or unstable toPx calls
 */
@Composable
fun AnimatedTree(
    stage: TreeStage,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    treeWithered: Boolean = false,
    displaySizeNormal: Dp = 220.dp,
    displaySizeWithered: Dp = 260.dp
) {
    // resource mapping (ensure these exist)
    val healthyRes = when (stage) {
        TreeStage.SEED -> R.drawable.ic_tree_seed
        TreeStage.SAPLING -> R.drawable.ic_tree_sprout
        TreeStage.YOUNG -> R.drawable.ic_tree_small
        TreeStage.FULL -> R.drawable.ic_tree_full
    }
    val deadRes = when (stage) {
        TreeStage.SEED -> R.drawable.ic_tree_dead_seed
        TreeStage.SAPLING -> R.drawable.ic_tree_dead_sprout
        TreeStage.YOUNG -> R.drawable.ic_tree_dead_small
        TreeStage.FULL -> R.drawable.ic_tree_dead_full
    }

    val spriteRes = if (treeWithered) deadRes else healthyRes
    val context = LocalContext.current
    val density = LocalDensity.current

    // Query bitmap size without loading full bitmap
    val intrinsicPxPair = remember(spriteRes) {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, spriteRes, opts)
        val w = if (opts.outWidth > 0) opts.outWidth else 1
        val h = if (opts.outHeight > 0) opts.outHeight else 1
        Pair(w, h)
    }

    // compute display px deterministically
    val displayDp = if (treeWithered) displaySizeWithered else displaySizeNormal
    val displayPx = with(density) { displayDp.toPx() }

    // intrinsic width in px
    val intrinsicPx = intrinsicPxPair.first.toFloat().coerceAtLeast(1f)

    // compute scaleUp (only upscale, don't downscale intrinsic)
    val scaleUpFactor = (displayPx / intrinsicPx).coerceAtLeast(1f)

    LaunchedEffect(spriteRes) {
        Log.d("AnimatedTree", "spriteRes=$spriteRes intrinsic=${intrinsicPxPair.first}x${intrinsicPxPair.second} scaleUp=${"%.2f".format(scaleUpFactor)}")
    }

    // animations
    val targetScaleBase = when {
        treeWithered -> 1.08f
        animate -> 1.03f
        else -> 1f
    }
    val animatedBase by animateFloatAsState(
        targetValue = targetScaleBase,
        animationSpec = tween(durationMillis = 700),
        label = ""
    )

    val sway by rememberInfiniteTransition().animateFloat(
        initialValue = if (animate && !treeWithered) -2f else 0f,
        targetValue = if (animate && !treeWithered) 2f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (animate && !treeWithered) 2600 else 1),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val finalScale = animatedBase * scaleUpFactor
    val alpha = 1f
    val displayDpSize = displayDp

    Box(
        modifier = modifier
            .size(displayDpSize)
            .graphicsLayer {
                scaleX = finalScale
                scaleY = finalScale
                rotationZ = sway
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        val painter = painterResource(id = spriteRes)
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.matchParentSize()
        )
    }
}
