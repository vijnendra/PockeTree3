package com.pocketree.pocketree.ui.timer

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
import com.pocketree.pocketree.ui.components.CollapseAnimation
import com.pocketree.pocketree.ui.theme.LightGreen
import com.pocketree.pocketree.ui.theme.SoftMint
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var running by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(25 * 60) }
    var finished by remember { mutableStateOf(false) }
    var treeWithered by remember { mutableStateOf(false) }

    // Detect minimize / app switch / lock screen
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && running) {
                treeWithered = true
                running = false
                finished = false

                val elapsed = (25 * 60) - secondsLeft
                viewModel.endSessionAndSave(
                    wasWithered = true,
                    elapsedSecondsOverride = elapsed.toLong()
                )

                secondsLeft = 0
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = SoftMint
    ) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            TopAppBar(
                title = { Text("Pocketree", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    Text(
                        "Forest",
                        modifier = Modifier
                            .padding(10.dp)
                            .clickable { navController.navigate("forest") }
                    )
                }
            )

            // ---------------- TREE + TIMER CARD ----------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {

                Column(
                    Modifier.fillMaxSize().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    val treeStage =
                        when {
                            treeWithered -> TreeStage.SEED
                            finished -> TreeStage.FULL
                            running && secondsLeft < 10 * 60 -> TreeStage.YOUNG
                            running -> TreeStage.SAPLING
                            else -> TreeStage.SEED
                        }

                    // TREE BOX
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CollapseAnimation(trigger = treeWithered) {
                            AnimatedTree(stage = treeStage)
                        }
                        WitherOverlay(visible = treeWithered)
                    }

                    // TIMER TEXT
                    Text(
                        text = formatTime(secondsLeft),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // BUTTONS
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                        Button(
                            onClick = {
                                if (!running) viewModel.startSession()
                                running = !running
                                treeWithered = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
                        ) {
                            Text(if (running) "Pause" else "Start")
                        }

                        OutlinedButton(
                            onClick = {
                                running = false
                                finished = false
                                treeWithered = false
                                secondsLeft = 25 * 60
                                viewModel.cancelSession()
                            }
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }

            // ---------------- CUSTOM TIMER ----------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                var customMinutes by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("Session length")

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        OutlinedTextField(
                            value = customMinutes,
                            onValueChange = {
                                customMinutes = it.filter { c -> c.isDigit() }
                            },
                            modifier = Modifier.width(80.dp),
                            label = { Text("Minutes") },
                            singleLine = true
                        )

                        Button(onClick = {
                            if (customMinutes.isNotEmpty()) {
                                secondsLeft = customMinutes.toInt() * 60
                                finished = false
                                treeWithered = false
                            }
                        }) {
                            Text("Set")
                        }
                    }
                }
            }
        }
    }

    // ----------- TIMER LOOP -----------
    LaunchedEffect(running) {
        while (running && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }

        if (secondsLeft <= 0 && running) {
            running = false
            finished = true

            viewModel.endSessionAndSave(
                wasWithered = false,
                elapsedSecondsOverride = (25 * 60).toLong()
            )
        }
    }
}

// ---------------- Helper ----------------
private fun formatTime(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val mm = s / 60
    val ss = s % 60
    return "%02d:%02d".format(mm, ss)
}
