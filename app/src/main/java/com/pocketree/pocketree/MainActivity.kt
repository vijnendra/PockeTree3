package com.pocketree.pocketree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pocketree.pocketree.ui.theme.PocketreeTheme
import com.pocketree.pocketree.ui.timer.TimerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketreeTheme {
                TimerScreen()
            }
        }
    }
}
