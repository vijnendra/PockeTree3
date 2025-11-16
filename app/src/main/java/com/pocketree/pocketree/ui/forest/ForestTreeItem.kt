package com.pocketree.pocketree.ui.forest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pocketree.pocketree.R

@Composable
fun ForestTreeItem(
    duration: Int,
    wasWithered: Boolean
) {
    Card(
        modifier = Modifier
            .size(110.dp)
            .padding(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_tree),
                contentDescription = "Tree",
                modifier = Modifier
                    .size(70.dp)
                    .alpha(if (wasWithered) 0.4f else 1f)
            )

            if (wasWithered) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "X",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
