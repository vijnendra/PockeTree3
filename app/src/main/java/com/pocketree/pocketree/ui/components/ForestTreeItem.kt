package com.pocketree.pocketree.ui.forest

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
import com.pocketree.pocketree.data.model.TreeSession
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp

/**
 * Displays a single tree tile in the forest grid.
 *
 * Rules:
 *  - If session.isWithered == true -> show appropriate *dead* sprite determined by progress (duration/planned).
 *  - Otherwise -> show healthy sprite determined by progress.
 *  - progress = duration / planned (clamped). If planned == 0, fall back to duration thresholds:
 *      - >= 60 min -> treat as long/full,
 *      - else progress from 0..1 by using small heuristics.
 */
@Composable
fun ForestTreeItem(session: TreeSession) {
    // compute progress safely
    val duration = session.durationMinutes.coerceAtLeast(0)
    val planned = session.plannedMinutes.coerceAtLeast(0)

    val progress: Float = when {
        planned > 0 -> (duration.toFloat() / planned.toFloat()).coerceIn(0f, 1f)
        duration >= 60 -> 1f // treat long durations as full
        else -> (duration.toFloat() / 60f).coerceIn(0f, 1f) // spread 0..60min into 0..1
    }

    // stage thresholds — keep consistent with TimerScreen logic
    val stageIndex = when {
        progress >= 0.85f -> 3   // FULL
        progress >= 0.45f -> 2   // YOUNG
        progress >= 0.15f -> 1   // SAPLING
        else -> 0                 // SEED
    }

    // pick drawable resource ids (healthy vs dead)
    val resId = if (session.isWithered) {
        when (stageIndex) {
            0 -> R.drawable.ic_tree_dead_seed
            1 -> R.drawable.ic_tree_dead_sprout
            2 -> R.drawable.ic_tree_dead_small
            else -> R.drawable.ic_tree_dead_full
        }
    } else {
        when (stageIndex) {
            0 -> R.drawable.ic_tree_seed
            1 -> R.drawable.ic_tree_sprout
            2 -> R.drawable.ic_tree_young
            else -> R.drawable.ic_tree_full
        }
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // duration text
            Text(
                text = "${duration} min",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp
            )

            // optional: show the date or small indicator if you want (commented)
            // Text(text = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(session.startTime)), style = MaterialTheme.typography.bodySmall)
        }
    }
}
