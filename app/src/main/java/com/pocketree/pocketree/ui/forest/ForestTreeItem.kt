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

@Composable
fun ForestTreeItem(
    duration: Int,
    wasWithered: Boolean
) {
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
                painter = painterResource(
                    if (wasWithered)
                        R.drawable.ic_tree_red_seed     // withered → seed only
                    else
                        R.drawable.ic_tree_red_full     // completed → full fruit tree
                ),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text("$duration min")
        }
    }
}
