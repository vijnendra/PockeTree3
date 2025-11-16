package com.pocketree.pocketree.ui.forest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ForestScreen(viewModel: ForestViewModel) {

    val sessions by viewModel.displaySessions.collectAsState()
    val streak by viewModel.streak.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Day", "Month", "Year", "Lifetime")

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Text("Your Forest", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(6.dp))

        Text("🔥 Streak: $streak days", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(12.dp))

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

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            items(sessions) { session ->
                ForestTreeItem(
                    duration = session.durationMinutes,
                    wasWithered = session.wasWithered
                )
            }
        }
    }
}
