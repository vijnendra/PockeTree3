package com.pocketree.pocketree.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketree.pocketree.PockeTreeApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Simple ViewModel that stores start timestamp and saves sessions via repository.
 * Uses PockeTreeApp.instance.repository (created earlier).
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PockeTreeApp.instance.repository

    // timestamp when current session started (millis). 0 when none running.
    private var sessionStartMs: Long = 0L

    fun startSession() {
        sessionStartMs = System.currentTimeMillis()
    }

    fun cancelSession() {
        sessionStartMs = 0L
    }

    /**
     * End the current session and save to DB.
     * - durationMinutes: derived from elapsedMs if provided, otherwise computed from start time.
     * - wasWithered: true if the session ended because of background/withering.
     */
    fun endSessionAndSave(wasWithered: Boolean, elapsedSecondsOverride: Long? = null) {
        val start = sessionStartMs
        val now = System.currentTimeMillis()

        // compute elapsed in seconds: prefer override (useful if you zeroed timer)
        val elapsedSec = elapsedSecondsOverride ?: if (start > 0L) {
            TimeUnit.MILLISECONDS.toSeconds(now - start)
        } else {
            0L
        }

        val minutes = (elapsedSec / 60L).toInt()

        // reset session start
        sessionStartMs = 0L

        // Save to DB on IO dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            // repository expects durationMinutes as Int, and wasWithered flag
            repository.insertSession(
                durationMinutes = minutes,
                wasWithered = wasWithered
            )
        }
    }
}
