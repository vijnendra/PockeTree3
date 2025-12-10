package com.pocketree.pocketree.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.pocketree.pocketree.data.model.TreeSession
import com.pocketree.pocketree.data.repository.TreeSessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Single source of truth for timer state.
 * - Exposes StateFlows the UI collects.
 * - Keeps ticking in viewModelScope so navigation won't cancel it.
 * - Observes ProcessLifecycleOwner to detect app backgrounding and mark withered sessions.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    // repository (adapt to your repository constructor if different)
    private val repo = TreeSessionRepository(application)

    // ---- state exposed to UI ----
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _sessionSeconds = MutableStateFlow(25 * 60)
    val sessionSeconds: StateFlow<Int> = _sessionSeconds

    private val _secondsLeft = MutableStateFlow(_sessionSeconds.value)
    val secondsLeft: StateFlow<Int> = _secondsLeft

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private val _isWithered = MutableStateFlow(false)
    val isWithered: StateFlow<Boolean> = _isWithered

    private val _lastStageBeforeWither = MutableStateFlow(com.pocketree.pocketree.ui.components.TreeStage.SEED)
    val lastStageBeforeWither: StateFlow<com.pocketree.pocketree.ui.components.TreeStage> = _lastStageBeforeWither

    // internal ticking job
    private var tickerJob: Job? = null

    init {
        // Observe app lifecycle using ProcessLifecycleOwner.
        // When app goes to background (ON_STOP) and timer is running -> wither.
        val lifecycleOwner = ProcessLifecycleOwner.get()
        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (_isRunning.value) {
                        // compute elapsed based on secondsLeft
                        val elapsed = _sessionSeconds.value - _secondsLeft.value
                        val plannedMins = (_sessionSeconds.value / 60).coerceAtLeast(1)

                        // determine approximate stage for lastStageBeforeWither
                        val total = _sessionSeconds.value.coerceAtLeast(1)
                        val progress = 1f - (_secondsLeft.value.coerceAtLeast(0) / total.toFloat())
                        val isLong = _sessionSeconds.value >= 3600
                        _lastStageBeforeWither.value = stageFromProgress(progress, isLong, _isFinished.value, _isRunning.value)

                        // mark withered
                        _isWithered.value = true
                        _isRunning.value = false
                        _isFinished.value = false

                        // save in repo
                        endSessionAndSaveInternal(withered = true, elapsedSecondsOverride = elapsed.toLong(), plannedMinutes = plannedMins)

                        // zero out
                        _secondsLeft.value = 0
                    }
                }
                else -> { /* no-op */ }
            }
        })
    }

    // compute stage helper (same thresholds as UI)
    private fun stageFromProgress(progress: Float, isLong: Boolean, finishedFlag: Boolean, runningFlag: Boolean): com.pocketree.pocketree.ui.components.TreeStage {
        return when {
            finishedFlag -> com.pocketree.pocketree.ui.components.TreeStage.FULL
            runningFlag && progress >= 0.85f -> com.pocketree.pocketree.ui.components.TreeStage.FULL
            runningFlag && progress >= 0.45f -> com.pocketree.pocketree.ui.components.TreeStage.YOUNG
            runningFlag && progress >= 0.15f -> com.pocketree.pocketree.ui.components.TreeStage.SAPLING
            runningFlag -> com.pocketree.pocketree.ui.components.TreeStage.SEED
            else -> com.pocketree.pocketree.ui.components.TreeStage.SEED
        }
    }

    // -------- public control API the UI calls --------

    /** Start the session. plannedMinutes must be >= 1. */
    fun startSession(plannedMinutes: Int) {
        val secs = plannedMinutes.coerceAtLeast(1) * 60
        _sessionSeconds.value = secs
        _secondsLeft.value = secs
        _isWithered.value = false
        _isFinished.value = false
        _lastStageBeforeWither.value = com.pocketree.pocketree.ui.components.TreeStage.SEED

        _isRunning.value = true
        startTickerIfNeeded()
        // store session start time internally for saving
        _sessionStartMs = System.currentTimeMillis()
        _plannedMinutesForSave = plannedMinutes.coerceAtLeast(1)
    }

    fun pauseSession() {
        _isRunning.value = false
        stopTicker()
    }

    fun resumeSession() {
        if (!_isFinished.value && _secondsLeft.value > 0) {
            _isRunning.value = true
            startTickerIfNeeded()
        }
    }

    fun resetSession() {
        _isRunning.value = false
        _isFinished.value = false
        _isWithered.value = false
        _secondsLeft.value = _sessionSeconds.value
        stopTicker()
    }

    /** Called by UI when user explicitly sets new planned minutes (before starting) */
    fun setSessionMinutes(minutes: Int) {
        val mins = minutes.coerceAtLeast(1)
        _sessionSeconds.value = mins * 60
        _secondsLeft.value = mins * 60
    }

    // -------- ticker management --------
    private fun startTickerIfNeeded() {
        if (tickerJob != null && tickerJob?.isActive == true) return

        tickerJob = viewModelScope.launch {
            while (_isRunning.value && _secondsLeft.value > 0) {
                delay(1000L)
                _secondsLeft.value = (_secondsLeft.value - 1).coerceAtLeast(0)
            }

            if (_isRunning.value && _secondsLeft.value <= 0) {
                // natural completion
                _isRunning.value = false
                _isFinished.value = true

                // set final stage FULL
                _lastStageBeforeWither.value = com.pocketree.pocketree.ui.components.TreeStage.FULL

                // persist completed session
                val planned = _plannedMinutesForSave.coerceAtLeast(1)
                endSessionAndSaveInternal(withered = false, elapsedSecondsOverride = _sessionSeconds.value.toLong(), plannedMinutes = planned)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    // -------- saving to DB --------
    private var _sessionStartMs: Long = 0L
    private var _plannedMinutesForSave: Int = 0

    // internal save helper: uses repo.insert(TreeSession(...))
    private fun endSessionAndSaveInternal(withered: Boolean, elapsedSecondsOverride: Long? = null, plannedMinutes: Int) {
        // compute elapsed secs
        val start = _sessionStartMs
        val now = System.currentTimeMillis()
        val elapsedSec = elapsedSecondsOverride ?: if (start > 0L) TimeUnit.MILLISECONDS.toSeconds(now - start) else 0L

        val minutes = (elapsedSec / 60L).toInt()
        val finalDuration = if (withered) minutes.coerceAtMost(plannedMinutes) else plannedMinutes

        // reset
        _sessionStartMs = 0L

        viewModelScope.launch {
            repo.insert(
                TreeSession(
                    plannedMinutes = plannedMinutes,
                    durationMinutes = finalDuration,
                    startTime = start,
                    endTime = now,
                    isWithered = withered
                )
            )
        }
    }

    // Backwards-compatible wrapper that UI code used earlier (keeps param names)
    fun endSessionAndSave(wasWithered: Boolean, elapsedSecondsOverride: Long? = null) {
        val planned = _plannedMinutesForSave.coerceAtLeast(1)
        endSessionAndSaveInternal(wasWithered, elapsedSecondsOverride, planned)
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
    }
}
