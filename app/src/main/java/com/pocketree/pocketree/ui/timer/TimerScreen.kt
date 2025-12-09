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
    // Collect VM state
    val isRunning by viewModel.isRunning.collectAsState()
    val sessionSeconds by viewModel.sessionSeconds.collectAsState()
    val secondsLeft by viewModel.secondsLeft.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val isWithered by viewModel.isWithered.collectAsState()
    val lastStageBeforeWither by viewModel.lastStageBeforeWither.collectAsState()

    // Local input state for minutes box
    var minutesInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pocketree", style = MaterialTheme.typography.titleLarge) },
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

            // ----------------- PERSISTENT WITHER BANNER (SIMPLE) -----------------
            if (isWithered) {
                WitheredBanner()
                Spacer(Modifier.height(12.dp))
            }

            // ----------------- MAIN FOCUS CARD -----------------
            FocusTreeCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                secondsLeft = secondsLeft,
                sessionSeconds = sessionSeconds,
                running = isRunning,
                finished = isFinished,
                treeWithered = isWithered,
                lastStageBeforeWither = lastStageBeforeWither,
                onStartPauseClick = {
                    if (!isRunning) viewModel.startSession(plannedMinutes = sessionSeconds / 60)
                    else viewModel.pauseSession()
                },
                onResetClick = {
                    viewModel.cancelSession()
                }
            )

            Spacer(Modifier.height(16.dp))

            // ----------------- SESSION LENGTH CARD -----------------
            SessionLengthCard(
                minutesText = minutesInput,
                onMinutesChange = { input -> minutesInput = input.filter { it.isDigit() } },
                onSetClick = {
                    if (minutesInput.isNotEmpty()) {
                        val mins = minutesInput.toInt().coerceAtLeast(1)
                        viewModel.setSessionMinutes(mins)
                    }
                }
            )
        }
    }
}

/* ----------------- WitheredBanner (minimal, no buttons) ----------------- */
@Composable
fun WitheredBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Your last tree withered",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Start a new session to grow a fresh one",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}

/* ----------------- FocusTreeCard ----------------- */
@Composable
fun FocusTreeCard(
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
    val isLongSession = sessionSeconds >= 60 * 60

    val runningStage = when {
        finished && isLongSession -> TreeStage.FULL
        finished && !isLongSession -> TreeStage.YOUNG
        running && progress >= 0.85f -> TreeStage.FULL
        running && progress >= 0.45f -> TreeStage.YOUNG
        running && progress >= 0.15f -> TreeStage.SAPLING
        running -> TreeStage.SEED
        else -> TreeStage.SEED
    }

    val visualStage = if (treeWithered) lastStageBeforeWither else runningStage

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
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
                // Show the correct sprite; animate only when running and not withered
                AnimatedTree(
                    stage = visualStage,
                    animate = running && !treeWithered,
                    treeWithered = treeWithered
                )
            }

            Text(
                text = formatTime(secondsLeft),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Session: ${sessionSeconds / 60} min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

/* ----------------- SessionLengthCard ----------------- */
@Composable
fun SessionLengthCard(
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
            Text(
                text = "Session length",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "longer sessions grow bigger trees",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No. of minutes",
                    modifier = Modifier.weight(1f)
                )

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

/* ----------------- Helper ----------------- */
private fun formatTime(seconds: Int): String {
    val mm = seconds / 60
    val ss = seconds % 60
    return "%02d:%02d".format(mm.coerceAtLeast(0), ss.coerceAtLeast(0))
}
