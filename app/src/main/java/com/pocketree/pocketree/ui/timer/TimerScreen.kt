package com.pocketree.pocketree.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimerScreen(vm: TimerViewModel) {

    val timeLeft = vm.timeLeft.observeAsState(initial = 0L)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Time left: ${(timeLeft.value / 1000)} s")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { vm.startSession() }) {
            Text("Start Session")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { vm.cancelSession() }) {
            Text("Cancel Session")
        }
    }
}
