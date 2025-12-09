package com.pocketree.pocketree.ui.timer

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.ProcessLifecycleOwner
import com.pocketree.pocketree.data.model.TreeSession
import com.pocketree.pocketree.data.repository.TreeSessionRepository
import com.pocketree.pocketree.ui.components.TreeStage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "TimerViewModel"

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TreeSessionRepository(application)

    // UI-observable state flows
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

    private val _lastStageBeforeWither = MutableStateFlow(TreeStage.SEED)
    val lastStageBeforeWither: StateFlow<TreeStage> = _lastStageBeforeWither

    private var countdownJob: Job? = null

    // planned minutes used for saving to DB
    private var plannedMinutesForCurrentSession: Int = 25

    // guard to avoid double-handling background event
    private var backgroundHandled = false

    // Declare observer before init so Kotlin knows it's initialized
    private val processLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP) {
            if (_isRunning.value) {
                Log.d(TAG, "ProcessLifecycleOwner ON_STOP detected, calling onAppBackgrounded()")
                onAppBackgrounded()
            }
        }
    }

    init {
        // Register observer to ProcessLifecycleOwner to handle app -> background.
        ProcessLifecycleOwner.get().lifecycle.addObserver(processLifecycleObserver)
    }

    /**
     * Start a session. plannedMinutes defaults to current sessionSeconds / 60.
     */
    fun startSession(plannedMinutes: Int = (_sessionSeconds.value / 60)) {
        if (_isRunning.value) return
        backgroundHandled = false
        plannedMinutesForCurrentSession = plannedMinutes.coerceAtLeast(1)
        _isWithered.value = false
        _isFinished.value = false
        _isRunning.value = true

        if (countdownJob?.isActive != true) {
            countdownJob = viewModelScope.launch {
                while (_isRunning.value && _secondsLeft.value > 0) {
                    delay(1000L)
                    _secondsLeft.value = _secondsLeft.value - 1
                }
                if (_isRunning.value && _secondsLeft.value <= 0) {
                    // Normal finish
                    _isRunning.value = false
                    _isFinished.value = true
                    endSessionAndSave(wasWithered = false, elapsedSecondsOverride = _sessionSeconds.value.toLong())
                }
            }
        }
    }

    fun pauseSession() {
        _isRunning.value = false
    }

    fun cancelSession() {
        _isRunning.value = false
        _isFinished.value = false
        _isWithered.value = false
        _lastStageBeforeWither.value = TreeStage.SEED
        _secondsLeft.value = _sessionSeconds.value
        plannedMinutesForCurrentSession = (_sessionSeconds.value / 60)
        backgroundHandled = false
        countdownJob?.cancel()
        countdownJob = null
    }

    fun clearWithered() {
        _isWithered.value = false
        _lastStageBeforeWither.value = TreeStage.SEED
        backgroundHandled = false
    }

    fun setSessionMinutes(mins: Int) {
        val m = mins.coerceAtLeast(1)
        _sessionSeconds.value = m * 60
        _secondsLeft.value = _sessionSeconds.value
        plannedMinutesForCurrentSession = m
    }

    /**
     * Called when the app process goes to background. This marks session as withered,
     * stops timer and saves a withered session to DB. Idempotent per session.
     */
    fun onAppBackgrounded() {
        if (!_isRunning.value) return
        if (backgroundHandled) return

        backgroundHandled = true

        val total = _sessionSeconds.value.coerceAtLeast(1)
        val progress = 1f - (_secondsLeft.value.coerceAtLeast(0) / total.toFloat())
        val isLong = _sessionSeconds.value >= 60 * 60

        _lastStageBeforeWither.value = when {
            progress >= 0.85f && isLong -> TreeStage.FULL
            progress >= 0.45f -> TreeStage.YOUNG
            progress >= 0.15f -> TreeStage.SAPLING
            else -> TreeStage.SEED
        }

        _isWithered.value = true
        _isRunning.value = false
        _isFinished.value = false

        val elapsed = (_sessionSeconds.value - _secondsLeft.value).toLong()
        endSessionAndSave(wasWithered = true, elapsedSecondsOverride = elapsed)
    }

    /**
     * End and persist a session. elapsedSecondsOverride must be in seconds.
     */
    fun endSessionAndSave(wasWithered: Boolean, elapsedSecondsOverride: Long? = null) {
        val elapsedSec = elapsedSecondsOverride ?: 0L
        val durationMin = (elapsedSec / 60L).toInt().coerceAtLeast(0)

        val finalDuration = if (wasWithered) durationMin.coerceAtMost(plannedMinutesForCurrentSession)
        else plannedMinutesForCurrentSession

        // reset
        _isRunning.value = false
        countdownJob?.cancel()
        countdownJob = null

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repo.insert(
                    TreeSession(
                        plannedMinutes = plannedMinutesForCurrentSession,
                        durationMinutes = finalDuration,
                        startTime = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(elapsedSec),
                        endTime = System.currentTimeMillis(),
                        isWithered = wasWithered
                    )
                )
                Log.d(TAG, "Saved session (withered=$wasWithered, durationMin=$finalDuration)")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving session", e)
            }
        }
    }

    override fun onCleared() {
        try {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(processLifecycleObserver)
        } catch (e: Exception) {
            // ignore
        }
        countdownJob?.cancel()
        super.onCleared()
    }
}
