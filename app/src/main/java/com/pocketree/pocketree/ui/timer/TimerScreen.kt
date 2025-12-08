package com.pocketree.pocketree.ui.timer

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

private const val PREFS_NAME = "pocketree_prefs"
private const val KEY_WITHERED_FLAG = "show_withered"
private const val KEY_WITHERED_STAGE = "last_withered_stage"

/**
 * TimerScreen.kt (minimal-banner version)
 *
 * - Minimal (least-invasive) banner when persisted withered tree exists:
 *   centered two-line message, no action buttons.
 * - Persisted wither is cleared when user starts/sets/resets.
 * - Wither overlay animation runs only for fresh wither events.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    fun saveWitheredStage(stage: TreeStage) {
        prefs.edit().putBoolean(KEY_WITHERED_FLAG, true)
            .putString(KEY_WITHERED_STAGE, stage.name)
            .apply()
    }
    fun clearWitheredFlag() {
        prefs.edit().putBoolean(KEY_WITHERED_FLAG, false)
            .remove(KEY_WITHERED_STAGE)
            .apply()
    }
    fun readWitheredStage(): TreeStage? {
        if (!prefs.getBoolean(KEY_WITHERED_FLAG, false)) return null
        val name = prefs.getString(KEY_WITHERED_STAGE, null) ?: return null
        return try { TreeStage.valueOf(name) } catch (e: Exception) { null }
    }

    // ------------------ UI / timer state ------------------
    var running by remember { mutableStateOf(false) }
    var sessionSeconds by remember { mutableStateOf(25 * 60) }
    var secondsLeft by remember { mutableStateOf(sessionSeconds) }
    var finished by remember { mutableStateOf(false) }

    var treeWithered by remember { mutableStateOf(false) }
    var persistedWithered by remember { mutableStateOf(false) }
    var lastStageBeforeWither by remember { mutableStateOf(TreeStage.SEED) }

    var customMinutes by remember { mutableStateOf("") }

    // Read persisted wither once on composition
    LaunchedEffect(Unit) {
        val persisted = readWitheredStage()
        if (persisted != null) {
            persistedWithered = true
            treeWithered = true
            lastStageBeforeWither = persisted
            secondsLeft = 0
            running = false
            finished = false
        }
    }

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

    // ------------------ Handle app background -> wither ------------------
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && running) {
                val total = sessionSeconds.coerceAtLeast(1)
                val progress = 1f - (secondsLeft.toFloat() / total.toFloat())
                val isLong = sessionSeconds >= (60 * 60)

                val nowStage = stageFromProgress(progress, isLong, finished, running)
                lastStageBeforeWither = nowStage

                treeWithered = true
                persistedWithered = true
                running = false
                finished = false

                val elapsed = (sessionSeconds - secondsLeft).coerceAtLeast(0)
                viewModel.endSessionAndSave(
                    wasWithered = true,
                    elapsedSecondsOverride = elapsed.toLong()
                )

                saveWitheredStage(nowStage)
                secondsLeft = 0
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // ------------------ Main UI ------------------
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
                            .padding(end = 12.dp)
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

            // ------------------ Minimal non-interactive banner (least invasive) ------------------
            if (persistedWithered) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your last tree withered",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Start a new session to grow a fresh one",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // ------------------ Focus tree card ------------------
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
                persistedWithered = persistedWithered,
                onStartPauseClick = {
                    if (!running) {
                        // clear persisted wither when user intentionally starts a new session
                        persistedWithered = false
                        clearWitheredFlag()
                        viewModel.startSession(sessionSeconds / 60)
                        finished = false
                        treeWithered = false
                    }
                    running = !running
                },
                onResetClick = {
                    running = false
                    finished = false
                    treeWithered = false
                    persistedWithered = false
                    lastStageBeforeWither = TreeStage.SEED
                    secondsLeft = sessionSeconds
                    viewModel.cancelSession()
                    clearWitheredFlag()
                }
            )

            Spacer(Modifier.height(16.dp))

            // ------------------ Session length card (MANDATORY layout) ------------------
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
                        persistedWithered = false
                        lastStageBeforeWither = TreeStage.SEED
                        clearWitheredFlag()
                    }
                },
                running = running
            )
        }
    }

    // ------------------ Timer loop ------------------
    LaunchedEffect(running, sessionSeconds) {
        while (running && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }

        if (!running) return@LaunchedEffect

        if (secondsLeft <= 0) {
            running = false
            finished = true

            lastStageBeforeWither = stageFromProgress(1f, sessionSeconds >= 3600, true, false)

            viewModel.endSessionAndSave(
                wasWithered = false,
                elapsedSecondsOverride = sessionSeconds.toLong()
            )

            persistedWithered = false
            clearWitheredFlag()
        }
    }
}

/* ------------------ FocusTreeCard (uses persistedWithered to skip overlay) ------------------ */
@Composable
private fun FocusTreeCard(
    modifier: Modifier,
    secondsLeft: Int,
    sessionSeconds: Int,
    running: Boolean,
    finished: Boolean,
    treeWithered: Boolean,
    lastStageBeforeWither: TreeStage,
    persistedWithered: Boolean,
    onStartPauseClick: () -> Unit,
    onResetClick: () -> Unit
) {
    val total = sessionSeconds.coerceAtLeast(1)
    val progress = 1f - (secondsLeft / total.toFloat())
    val isLongSession = sessionSeconds >= 3600

    val runningStage = when {
        finished && isLongSession -> TreeStage.FULL
        finished && !isLongSession -> TreeStage.YOUNG
        running && progress >= 0.85f -> TreeStage.FULL
        running && progress >= 0.45f -> TreeStage.YOUNG
        running && progress >= 0.15f -> TreeStage.SAPLING
        running -> TreeStage.SEED
        else -> TreeStage.SEED
    }

    val visualStage = if (treeWithered || persistedWithered) lastStageBeforeWither else runningStage

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
                AnimatedTree(
                    stage = visualStage,
                    animate = false,
                    treeWithered = (treeWithered || persistedWithered)
                )

                // Only show animated overlay when it's a fresh wither (not persisted).
                if (treeWithered && !persistedWithered) {
                    WitherOverlay(visible = true, holdMs = 900L)
                }
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
                ) { Text(if (running) "Pause" else "Start") }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onResetClick,
                    shape = RoundedCornerShape(50)
                ) { Text("Reset") }
            }
        }
    }
}

/* ------------------ SessionLengthCard (mandatory single-line minutes input) ------------------ */
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
            // Title & subtitle centered
            Text("Session length", style = MaterialTheme.typography.bodyLarge)
            Text(
                "longer sessions grow bigger trees",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row: [left label]  [center input single-line]  [divider]  [Set button]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left label
                Text(
                    text = "No. of minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Center: single-line input, placeholder used to avoid two-line label wrap
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = onMinutesChange,
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    maxLines = 1,
                    placeholder = { Text("Minutes") }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Set button on right (visible & fixed min width)
                Button(
                    onClick = onSetClick,
                    enabled = minutesText.isNotEmpty() && !running,
                    modifier = Modifier.defaultMinSize(minWidth = 88.dp)
                ) {
                    Text("Set")
                }
            }
        }
    }
}

/* ------------------ Helpers ------------------ */
private fun formatTime(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val mm = s / 60
    val ss = s % 60
    return "%02d:%02d".format(mm, ss)
}
