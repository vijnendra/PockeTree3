package com.pocketree.pocketree.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pocketree.pocketree.R

enum class TreeStage {
    SEED, SAPLING, YOUNG, FULL
}

/**
 * StaticTree: no animation, deterministic size.
 * This is a drop-in replacement while we debug animation issues.
 * Adjust displaySizeDp to change how large the tree appears.
 */
@Composable
fun AnimatedTree(
    stage: TreeStage,
    modifier: Modifier = Modifier,
    // keep these params for compatibility with existing calls
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

    // fixed display size — tweak this. 220.dp is a good starting point for 512x512 art.
    val displaySizeDp = if (treeWithered) 260.dp else 220.dp

    Box(modifier = modifier.size(displaySizeDp), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = res),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )
    }
}
