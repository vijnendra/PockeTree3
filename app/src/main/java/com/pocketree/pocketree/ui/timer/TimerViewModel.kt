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
    val plannedDurationMs = 25 * 60 * 1000L


    fun startSession() {
        startTs = System.currentTimeMillis()
        timer = object : CountDownTimer(plannedDurationMs, 1000) {
            override fun onTick(msLeft: Long) {
                _timeLeft.postValue(msLeft)
            }

            override fun onFinish() {
                saveSession(success = true)
            }
        }.start()
    }

    fun cancelSession() {
        timer?.cancel()
        saveSession(success = false)
    }

    private fun saveSession(success: Boolean) {
        val endTs = System.currentTimeMillis()
        val minutes = ((endTs - startTs) / 60000).toInt().coerceAtLeast(0)

        val session = FocusSession(
            durationMinutes = minutes,
            timestamp = endTs,
            completed = success,
            treeType = if (success) "oak" else "withered"
        )

        viewModelScope.launch {
            repo.addSession(session)
        }
    }
}
