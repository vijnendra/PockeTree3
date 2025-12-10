package com.pocketree.pocketree.ui.forest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketree.pocketree.data.model.TreeSession
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForestScreen(viewModel: ForestViewModel = viewModel()) {

    // collect flows from VM
    val sessions by viewModel.displaySessions.collectAsState()
    val streak by viewModel.streak.collectAsState()

    // new summary flows
    val completedCount by viewModel.completedCount.collectAsState()
    val witheredCount by viewModel.witheredCount.collectAsState()
    val totalMinutes by viewModel.totalMinutesFocused.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Day", "Month", "Year", "Lifetime")

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Title row
        Text("Your Forest", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))

        // Streak + small summary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("🔥 Streak: $streak days", style = MaterialTheme.typography.titleMedium)
            }

            // compact summary: total minutes + completed/withered pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                // total minutes (simple text)
                Text(
                    text = "$totalMinutes min",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // small pill showing completed/withered counts (✓ / ✕)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .height(32.dp)
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$completedCount ✓",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$witheredCount ✕",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.95f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, text ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        viewModel.setTab(index)
                    },
                    text = { Text(text) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Sessions grid: pass the entire session object to ForestTreeItem(session)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sessions) { session: TreeSession ->
                ForestTreeItem(session = session)
            }
        }
    }
}
