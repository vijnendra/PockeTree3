package com.pocketree.pocketree

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pocketree.pocketree.ui.theme.PockeTreeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PockeTreeTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val ctx = LocalContext.current
    Button(
        onClick = { ctx.startActivity(Intent(ctx, TimerActivity::class.java)) },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Open Timer Screen")
    }
}
