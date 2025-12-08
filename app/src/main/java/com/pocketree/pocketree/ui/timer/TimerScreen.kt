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
    var sessionSeconds by remember { mutableStateOf(25 * 60) } // Int seconds
    var secondsLeft by remember { mutableStateOf(sessionSeconds) }
    var finished by remember { mutableStateOf(false) }
    var treeWithered by remember { mutableStateOf(false) }
    var lastStageBeforeWither by remember { mutableStateOf(TreeStage.SEED) }
    var customMinutes by remember { mutableStateOf("") }

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    fun stageFromProgress(progress: Float, isLongSession: Boolean, isFinished: Boolean, isRunning: Boolean): TreeStage {
        return when {
            isFinished && isLongSession -> TreeStage.FULL
            isFinished && !isLongSession -> TreeStage.YOUNG
            isRunning && progress >= 0.85f -> TreeStage.FULL
            isRunning && progress >= 0.45f -> TreeStage.YOUNG
            isRunning && progress >= 0.15f -> TreeStage.SAPLING
            isRunning -> TreeStage.SEED
            else -> TreeStage.SEED
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && running) {
                val total = sessionSeconds.coerceAtLeast(1)
                val progress = 1f - (secondsLeft.coerceAtLeast(0) / total.toFloat())
                val isLong = sessionSeconds >= (60 * 60)

                val stageNow = stageFromProgress(progress, isLong, finished, running)
                lastStageBeforeWither = stageNow

                treeWithered = true
                running = false
                finished = false

                // elapsed seconds while app was running until background
                val elapsed = (sessionSeconds - secondsLeft).coerceAtLeast(0)
                viewModel.endSessionAndSave(
                    wasWithered = true,
                    elapsedSecondsOverride = elapsed.toLong()
                )

                // freeze timer visually
                secondsLeft = 0
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
                        // pass planned minutes to ViewModel API (it expects minutes)
                        val plannedMinutes = (sessionSeconds / 60).coerceAtLeast(1)
                        viewModel.startSession(plannedMinutes)
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
                },
                onWitherAnimationFinished = {
                    treeWithered = false
                }
            )

            Spacer(Modifier.height(16.dp))

            SessionLengthCard(
                minutesText = customMinutes,
                onMinutesChange = { input -> customMinutes = input.filter { it.isDigit() } },
                onSetClick = {
                    if (customMinutes.isNotEmpty()) {
                        val mins = customMinutes.toInt().coerceAtLeast(1)
                        sessionSeconds = mins * 60
                        secondsLeft = sessionSeconds
                        finished = false
                        treeWithered = false
                        lastStageBeforeWither = TreeStage.SEED
                    }
                },
                running = running
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

            val total = sessionSeconds.coerceAtLeast(1)
            val progress = 1f - (secondsLeft.coerceAtLeast(0) / total.toFloat())
            val isLong = sessionSeconds >= (60 * 60)

            lastStageBeforeWither = stageFromProgress(progress, isLong, true, false)

            // Save finished session — pass total planned seconds as elapsed override so ViewModel computes duration correctly.
            viewModel.endSessionAndSave(
                wasWithered = false,
                elapsedSecondsOverride = sessionSeconds.toLong()
            )
        }
    }
}

/* ---------------- FocusTreeCard (NO CollapseAnimation) ---------------- */
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
    onResetClick: () -> Unit,
    onWitherAnimationFinished: (() -> Unit)? = null
) {
    val total = sessionSeconds.coerceAtLeast(1)
    val progress = 1f - (secondsLeft.coerceAtLeast(0) / total.toFloat())
    val isLongSession = sessionSeconds >= (60 * 60)

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Direct static drawing of tree; collapse animation removed
                AnimatedTree(
                    stage = visualStage,
                    animate = false,
                    treeWithered = treeWithered
                )

                // wither overlay still present (red X). Remove this call too if you want no overlay.
                WitherOverlay(
                    visible = treeWithered,
                    holdMs = 900L,
                    onFinished = { onWitherAnimationFinished?.invoke() }
                )
            }

            Text(
                text = formatTime(secondsLeft),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Session: ${sessionSeconds / 60} min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onStartPauseClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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

/* ---------------- SessionLengthCard ---------------- */
@Composable
private fun SessionLengthCard(
    minutesText: String,
    onMinutesChange: (String) -> Unit,
    onSetClick: () -> Unit,
    running: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "No. of minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = minutesText,
                    onValueChange = onMinutesChange,
                    modifier = Modifier.width(84.dp),
                    singleLine = true,
                    label = { Text("Minutes") }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                )

                Spacer(modifier = Modifier.width(12.dp))

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
