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
import com.pocketree.pocketree.ui.components.TreeStage
import com.pocketree.pocketree.ui.theme.LightGreen
import com.pocketree.pocketree.ui.theme.SoftMint
import com.pocketree.pocketree.ui.components.AnimatedTree


import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier
) {
    var running by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(25 * 60) } // default 25 min
    var finished by remember { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize(), color = SoftMint) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopAppBar(title = { Text("Pocketree", style = MaterialTheme.typography.titleLarge) }, actions = {})

            // Center card containing timer and tree
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    Modifier.fillMaxSize().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Animated tree area
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        AnimatedTree(
                            stage = if (finished) TreeStage.FULL else TreeStage.SEED,
                            modifier = Modifier
                        )

                    }

                    // Timer text / controls
                    Text(
                        text = formatTime(secondsLeft),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                running = !running
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
                        ) {
                            Text(if (running) "Pause" else "Start")
                        }

                        OutlinedButton(onClick = {
                            running = false
                            secondsLeft = 25 * 60
                            finished = false
                        }) {
                            Text("Reset")
                        }
                    }
                }
            }

            // Footer card: actions / quick presets
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Session length")
                    Row {
                        listOf(15, 25, 45).forEach { mins ->
                            Text(
                                "$mins",
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clickable {
                                        secondsLeft = mins * 60
                                        finished = false
                                    }
                                    .padding(6.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
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
        if (secondsLeft <= 0 && running) {
            // session finished
            running = false
            finished = true
            // trigger save to DB / award points — hook here
        }
    }
}

private fun formatTime(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val mm = s / 60
    val ss = s % 60
    return "%02d:%02d".format(mm, ss)
}
