package com.pocketree.pocketree.ui.forest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForestScreen(viewModel: ForestViewModel = viewModel()) {
    // collect flows from your existing ViewModel
    val sessions by viewModel.displaySessions.collectAsState()
    val streak by viewModel.streak.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Day", "Month", "Year", "Lifetime")

    // Summary calculations
    val totalMinutes = sessions.sumOf { it.durationMinutes }
    val failedCount = sessions.count { it.isWithered }    // <--- use isWithered
    val successCount = sessions.size - failedCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Forest", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥 Streak: ", style = MaterialTheme.typography.titleMedium)
                Text("$streak days", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$totalMinutes min", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(12.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$successCount ✓  $failedCount ✕",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
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

        // If empty, show empty state; otherwise show grid
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No trees yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Complete a focus session to grow trees in your forest",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
            ) {
                items(
                    items = sessions,
                    key = { it.id }
                ) { session ->
                    // map to correct properties from your model (isWithered)
                    ForestTreeItem(
                        duration = session.durationMinutes,
                        isWithered = session.isWithered,
                        startTime = session.startTime
                    )
                }
            }
        }
    }
}
