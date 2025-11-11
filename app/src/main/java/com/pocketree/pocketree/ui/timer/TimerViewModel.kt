package com.pocketree.pocketree.ui.timer

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pocketree.pocketree.data.model.FocusSession
import com.pocketree.pocketree.data.repo.FocusRepository
import kotlinx.coroutines.launch

class TimerViewModel(
    private val repo: FocusRepository,
    app: Application
) : AndroidViewModel(app) {

    private val _timeLeft = MutableLiveData<Long>()
    val timeLeft: LiveData<Long> get() = _timeLeft

    private var timer: CountDownTimer? = null
    private var startTs: Long = 0
    private var durationMs: Long = 0
    private var finished = false

    // total planned duration (25 min for testing)
    val plannedDurationMs = 25 * 60 * 1000L

    /** start the timer **/
    fun startSession() {
        startTs = System.currentTimeMillis()
        durationMs = plannedDurationMs
        finished = false
        timer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(msLeft: Long) {
                _timeLeft.value = msLeft
            }

            override fun onFinish() {
                finished = true
                saveSession(true)
            }
        }.start()
    }

    /** cancel timer when user leaves early **/
    fun cancelSessionAsFailed() {
        timer?.cancel()
        saveSession(false)
    }

    private fun saveSession(success: Boolean) {
        val endTs = System.currentTimeMillis()
        val session = FocusSession(
            startTs = startTs,
            endTs = endTs,
            durationSec = ((endTs - startTs) / 1000).toInt(),
            completed = success,
            treeType = if (success) "oak" else null
        )
        viewModelScope.launch {
            repo.save(session)
        }
    }
}
