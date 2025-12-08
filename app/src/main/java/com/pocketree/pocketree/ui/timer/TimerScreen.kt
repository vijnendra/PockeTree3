package com.pocketree.pocketree.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pocketree.pocketree.ui.components.AnimatedTree
import com.pocketree.pocketree.ui.components.TreeStage
import com.pocketree.pocketree.ui.components.WitherOverlay
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var running by remember { mutableStateOf(false) }
    var sessionSeconds by remember { mutableStateOf(25 * 60) }
    var secondsLeft by remember { mutableStateOf(sessionSeconds) }

    var finished by remember { mutableStateOf(false) }
    var treeWithered by remember { mutableStateOf(false) }

    var lastStageBeforeWither by remember { mutableStateOf(TreeStage.SEED) }

    var customMinutes by remember { mutableStateOf("") }

    fun stageFromProgress(progress: Float, isLong: Boolean, finished: Boolean, running: Boolean): TreeStage {
        return when {
            finished && isLong -> TreeStage.FULL
            finished && !isLong -> TreeStage.YOUNG
            running && progress >= 0.85f -> TreeStage.FULL
            running && progress >= 0.45f -> TreeStage.YOUNG
            running && progress >= 0.15f -> TreeStage.SAPLING
            running -> TreeStage.SEED
            else -> TreeStage.SEED
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle, running, sessionSeconds, secondsLeft) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && running) {
                val appState = ProcessLifecycleOwner.get().lifecycle.currentState
                val appBackgrounded = !appState.isAtLeast(Lifecycle.State.STARTED)


                if (appBackgrounded) {
                    val total = sessionSeconds.coerceAtLeast(1)
                    val progress = 1f - (secondsLeft.coerceAtLeast(0) / total.toFloat())
                    val longSession = sessionSeconds >= 3600

                    lastStageBeforeWither = stageFromProgress(progress, longSession, finished, running)

                    treeWithered = true
                    running = false
                    finished = false

                    val elapsed = sessionSeconds - secondsLeft
                    viewModel.endSessionAndSave(
                        wasWithered = true,
                        elapsedSecondsOverride = elapsed.toLong()
                    )

                    secondsLeft = 0
                }
            }
        }

        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

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

            if (treeWithered && secondsLeft == 0) {
                WitheredBanner()
                Spacer(Modifier.height(12.dp))
            }

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
                        viewModel.startSession(planned = sessionSeconds / 60)
                        finished = false
                        treeWithered = false
                    }
                    running = !running
                },
                onResetClick = {
                    running = false
                    finished = false
                    treeWithered = false
                    lastStageBeforeWither = TreeStage.SEED
                    secondsLeft = sessionSeconds
                    viewModel.cancelSession()
                }
            )

            Spacer(Modifier.height(18.dp))

            SessionLengthCard(
                minutesText = customMinutes,
                onMinutesChange = { txt -> customMinutes = txt.filter { it.isDigit() } },
                onSetClick = {
                    if (customMinutes.isNotEmpty()) {
                        val mins = customMinutes.toInt().coerceAtLeast(1)
                        sessionSeconds = mins * 60
                        secondsLeft = sessionSeconds
                        finished = false
                        treeWithered = false
                        lastStageBeforeWither = TreeStage.SEED
                    }
                }
            )
        }
    }

    LaunchedEffect(running, sessionSeconds) {
        while (running && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }

        if (!running) return@LaunchedEffect

        if (secondsLeft <= 0) {
            running = false
            finished = true

            val longSession = sessionSeconds >= 3600
            lastStageBeforeWither = if (longSession) TreeStage.FULL else TreeStage.YOUNG

            viewModel.endSessionAndSave(
                wasWithered = false,
                elapsedSecondsOverride = sessionSeconds.toLong()
            )
        }
    }
}

@Composable
fun WitheredBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your last tree withered",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Start a new session to grow a fresh one",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

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
    val longSession = sessionSeconds >= 3600

    val runningStage =
        if (finished) {
            if (longSession) TreeStage.FULL else TreeStage.YOUNG
        } else when {
            progress >= 0.85f -> TreeStage.FULL
            progress >= 0.45f -> TreeStage.YOUNG
            progress >= 0.15f -> TreeStage.SAPLING
            else -> TreeStage.SEED
        }

    val stageToShow = if (treeWithered) lastStageBeforeWither else runningStage

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
                    animate = false,
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

private fun formatTime(seconds: Int): String {
    val mm = seconds / 60
    val ss = seconds % 60
    return "%02d:%02d".format(mm, ss)
}
