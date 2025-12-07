package com.pocketree.pocketree.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pocketree.pocketree.R

@Composable
fun ForestTreeItem(
    duration: Int,
    wasWithered: Boolean
) {
    // Determine "stage index" from duration (minutes). This mirrors the thresholds in TimerScreen.
    val stage = when {
        duration >= 60 -> "full"
        duration >= 20 -> "young"
        duration >= 5 -> "small"
        duration >= 1 -> "sprout"
        else -> "seed"
    }

    // If withered, pick the dead sprite corresponding to that stage.
    val drawable = if (wasWithered) {
        when (stage) {
            "full" -> R.drawable.ic_tree_dead_full
            "young" -> R.drawable.ic_tree_dead_small
            "small" -> R.drawable.ic_tree_dead_small
            "sprout" -> R.drawable.ic_tree_dead_sprout
            else -> R.drawable.ic_tree_dead_seed
        }
    } else {
        when (stage) {
            "full" -> R.drawable.ic_tree_full
            "young" -> R.drawable.ic_tree_young
            "small" -> R.drawable.ic_tree_small
            "sprout" -> R.drawable.ic_tree_sprout
            else -> R.drawable.ic_tree_seed
        }
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(90.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text("$duration min")
        }
    }
}
