package com.pocketree.pocketree.ui.forest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pocketree.pocketree.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Small card used in the Forest grid.
 * Shows the correct sprite (withered/healthy), duration and optional date.
 */
@Composable
fun ForestTreeItem(
    duration: Int,
    isWithered: Boolean,
    startTime: Long
) {
    val dateText = try {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        sdf.format(Date(startTime))
    } catch (e: Exception) {
        ""
    }

    Surface(
        modifier = Modifier
            .padding(8.dp)
            .size(width = 110.dp, height = 110.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                val spriteRes = if (isWithered) {
                    when {
                        duration >= 60 -> R.drawable.ic_tree_dead_full
                        duration >= 20 -> R.drawable.ic_tree_dead_medium
                        duration >= 5 -> R.drawable.ic_tree_dead_small
                        else -> R.drawable.ic_tree_dead_seed
                    }
                } else {
                    when {
                        duration >= 60 -> R.drawable.ic_tree_full
                        duration >= 20 -> R.drawable.ic_tree_young
                        duration >= 5 -> R.drawable.ic_tree_small
                        else -> R.drawable.ic_tree_seed
                    }
                }

                Image(
                    painter = painterResource(id = spriteRes),
                    contentDescription = null,
                    modifier = Modifier
                        .height(44.dp)
                        .wrapContentWidth()
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$duration min", style = MaterialTheme.typography.bodyMedium)
                if (dateText.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}
