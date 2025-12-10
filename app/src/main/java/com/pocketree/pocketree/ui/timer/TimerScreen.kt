package com.pocketree.pocketree.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pocketree.pocketree.ui.components.AnimatedTree
import com.pocketree.pocketree.ui.components.TreeStage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // ViewModel StateFlows
    val running by viewModel.isRunning.collectAsState()
    val sessionSeconds by viewModel.sessionSeconds.collectAsState()
    val secondsLeft by viewModel.secondsLeft.collectAsState()
    val finished by viewModel.isFinished.collectAsState()
    val treeWithered by viewModel.isWithered.collectAsState()
    val lastStageBeforeWither by viewModel.lastStageBeforeWither.collectAsState()

    var customMinutes by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PockeTree", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    Text(
                        text = "Forest",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { navController.navigate("forest") }
                    )
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // WITHER BANNER
            if (treeWithered && secondsLeft == 0) {
                WitheredBanner()
                Spacer(Modifier.height(12.dp))
            }

            // MAIN TREE CARD
            FocusTreeCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                secondsLeft = secondsLeft,
                sessionSeconds = sessionSeconds,
                running = running,
                finished = finished,
                treeWithered = treeWithered,
                lastStageBeforeWither = lastStageBeforeWither,

                onStartPauseClick = {
                    if (!running) {
                        // CASE 1 → BRAND NEW SESSION
                        if (secondsLeft == sessionSeconds) {
                            val plannedMins = (sessionSeconds / 60).coerceAtLeast(1)
                            viewModel.startSession(plannedMins)
                        }
                        // CASE 2 → RESUME AFTER PAUSE
                        // DO NOT reset secondsLeft
                        // DO NOT call startSession() again
                        viewModel.resumeSession()
                    } else {
                        viewModel.pauseSession()
                    }
                },

                onResetClick = {
                    viewModel.resetSession()
                }
            )

            Spacer(Modifier.height(16.dp))

            // SESSION LENGTH CARD
            SessionLengthCard(
                minutesText = customMinutes,
                onMinutesChange = { input -> customMinutes = input.filter { it.isDigit() } },
                onSetClick = {
                    if (customMinutes.isNotEmpty()) {
                        val mins = customMinutes.toInt().coerceAtLeast(1)
                        viewModel.setSessionMinutes(mins)
                    }
                }
            )
        }
    }
}

/* ---------------------- UI COMPONENTS ---------------------- */

@Composable
private fun WitheredBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Your last tree withered", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Start a new session to grow a fresh one",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FocusTreeCard(
    modifier: Modifier,
    secondsLeft: Int,
    sessionSeconds: Int,
    running: Boolean,
    finished: Boolean,
    treeWithered: Boolean,
    lastStageBeforeWither: TreeStage,
    onStartPauseClick: () -> Unit,
    onResetClick: () -> Unit
) {
    val total = sessionSeconds.coerceAtLeast(1)
    val progress = 1f - (secondsLeft.coerceAtLeast(0) / total.toFloat())

    val runningStage = when {
        progress >= 0.85f -> TreeStage.FULL
        progress >= 0.45f -> TreeStage.YOUNG
        progress >= 0.15f -> TreeStage.SAPLING
        else -> TreeStage.SEED
    }

    val stageToShow =
        if (treeWithered) lastStageBeforeWither
        else if (finished) TreeStage.FULL
        else runningStage

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedTree(
                    stage = stageToShow,
                    animate = !treeWithered && !finished, // sway only while growing
                    treeWithered = treeWithered
                )
            }

            Text(formatTime(secondsLeft), style = MaterialTheme.typography.titleLarge)
            Text(
                "Session: ${sessionSeconds / 60} min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onStartPauseClick,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(if (running) "Pause" else "Start")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onResetClick,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

@Composable
private fun SessionLengthCard(
    minutesText: String,
    onMinutesChange: (String) -> Unit,
    onSetClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Session length", style = MaterialTheme.typography.bodyLarge)
            Text(
                "longer sessions grow bigger trees",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text("No. of minutes", modifier = Modifier.weight(1f))

                OutlinedTextField(
                    value = minutesText,
                    onValueChange = onMinutesChange,
                    modifier = Modifier
                        .width(90.dp)
                        .height(56.dp),
                    singleLine = true,
                    label = { Text("Minutes") }
                )

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = onSetClick,
                    enabled = minutesText.isNotEmpty(),
                    modifier = Modifier.defaultMinSize(minWidth = 80.dp)
                ) {
                    Text("Set")
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mm = seconds / 60
    val ss = seconds % 60
    return "%02d:%02d".format(mm.coerceAtLeast(0), ss.coerceAtLeast(0))
}
