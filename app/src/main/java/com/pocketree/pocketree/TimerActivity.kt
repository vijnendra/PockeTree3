package com.pocketree.pocketree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pocketree.pocketree.data.db.AppDatabase
import com.pocketree.pocketree.data.repo.FocusRepository
import com.pocketree.pocketree.ui.theme.PockeTreeTheme
import com.pocketree.pocketree.ui.timer.TimerScreen
import com.pocketree.pocketree.ui.timer.TimerViewModel

class TimerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(this)
        val repo = FocusRepository(db.focusSessionDao())

        val factory = viewModelFactory {
            initializer { TimerViewModel(repo, application) }
        }

        val vm = ViewModelProvider(this, factory)[TimerViewModel::class.java]

        setContent {
            PockeTreeTheme {
                TimerScreen(vm)
            }
        }
    }
}
