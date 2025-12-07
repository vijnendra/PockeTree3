package com.pocketree.pocketree.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketree.pocketree.data.model.TreeSession
import com.pocketree.pocketree.data.repository.TreeSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TreeSessionRepository(application)

    private var sessionStartMs: Long = 0L
    private var plannedMinutes: Int = 0

    fun startSession(planned: Int) {
        sessionStartMs = System.currentTimeMillis()
        plannedMinutes = planned
    }

    fun cancelSession() {
        sessionStartMs = 0L
    }

    fun endSessionAndSave(wasWithered: Boolean, elapsedSecondsOverride: Long? = null) {
        val start = sessionStartMs
        val now = System.currentTimeMillis()

        val elapsedSec = elapsedSecondsOverride ?: TimeUnit.MILLISECONDS
            .toSeconds(now - start)

        val duration = (elapsedSec / 60).toInt().coerceAtLeast(0)
        val finalDuration =
            if (wasWithered) duration.coerceAtMost(plannedMinutes)
            else plannedMinutes

        sessionStartMs = 0L

        viewModelScope.launch(Dispatchers.IO) {
            repo.insert(
                TreeSession(
                    plannedMinutes = plannedMinutes,
                    durationMinutes = finalDuration,
                    startTime = start,
                    endTime = now,
                    isWithered = wasWithered
                )
            )
        }
    }
}
