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
    var maxSeconds by remember { mutableStateOf(25 * 60) }
    var finished by remember { mutableStateOf(false) }
    var treeWithered by remember { mutableStateOf(false) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && running) {
                treeWithered = true
                running = false
                finished = false

                val elapsed = maxSeconds - secondsLeft
                viewModel.endSessionAndSave(
                    wasWithered = true,
                    elapsedSecondsOverride = elapsed.toLong()
                )

                secondsLeft = 0
                maxSeconds = 0
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
                            running && secondsLeft < (maxSeconds * 0.4f) -> TreeStage.YOUNG
                            running -> TreeStage.SAPLING
                            else -> TreeStage.SEED
                        }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CollapseAnimation(trigger = treeWithered) {
                            AnimatedTree(stage = treeStage)
                        }
                        WitherOverlay(visible = treeWithered)
                    }

                    Text(formatTime(secondsLeft))

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                        Button(
                            onClick = {
                                if (!running) {
                                    val planned = (secondsLeft / 60).coerceAtLeast(1)
                                    viewModel.startSession(planned)
                                    maxSeconds = secondsLeft
                                }
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
                                maxSeconds = 25 * 60
                                viewModel.cancelSession()
                            }
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }

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
                                maxSeconds = secondsLeft
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

    LaunchedEffect(running) {
        while (running && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }

        if (!running) return@LaunchedEffect

        if (secondsLeft <= 0) {
            running = false
            finished = true

            viewModel.endSessionAndSave(
                wasWithered = false,
                elapsedSecondsOverride = maxSeconds.toLong()
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mm = seconds / 60
    val ss = seconds % 60
    return "%02d:%02d".format(mm.coerceAtLeast(0), ss.coerceAtLeast(0))
}
