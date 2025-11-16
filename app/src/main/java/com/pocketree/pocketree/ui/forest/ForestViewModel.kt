package com.pocketree.pocketree.ui.forest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pocketree.pocketree.data.model.FocusSession
import com.pocketree.pocketree.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class ForestViewModel(
    private val repository: SessionRepository
) : ViewModel() {

    private val _displaySessions = MutableStateFlow<List<FocusSession>>(emptyList())
    val displaySessions: StateFlow<List<FocusSession>> = _displaySessions

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    init {
        viewModelScope.launch {
            repository.getAllSessions().collect {
                loadDay()
                calculateStreak()
            }
        }
    }

    fun setTab(tab: Int) {
        when (tab) {
            0 -> loadDay()
            1 -> loadMonth()
            2 -> loadYear()
            3 -> loadLifetime()
        }
    }

    private fun todayKey(): String = format(Date())
    private fun format(date: Date): String =
        "%04d-%02d-%02d".format(
            Calendar.getInstance().apply { time = date }.get(Calendar.YEAR),
            Calendar.getInstance().apply { time = date }.get(Calendar.MONTH) + 1,
            Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_MONTH)
        )

    private fun loadDay() {
        viewModelScope.launch {
            val today = todayKey()
            _displaySessions.value = repository.getSessionsForDay(today)
        }
    }

    private fun loadMonth() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val m = cal.get(Calendar.MONTH) + 1
            val y = cal.get(Calendar.YEAR)

            _displaySessions.value = repository.getAllSessionsOnce().filter {
                val key = it.dayKey
                key.startsWith("%04d-%02d".format(y, m))
            }
        }
    }

    private fun loadYear() {
        viewModelScope.launch {
            val y = Calendar.getInstance().get(Calendar.YEAR)

            _displaySessions.value = repository.getAllSessionsOnce().filter {
                it.dayKey.startsWith("$y-")
            }
        }
    }

    private fun loadLifetime() {
        viewModelScope.launch {
            _displaySessions.value = repository.getAllSessionsOnce()
        }
    }

    private suspend fun calculateStreak() {
        val days = repository.getUniqueDays().toSet()
        if (days.isEmpty()) {
            _streak.value = 0
            return
        }

        var count = 0
        var checking = todayKey()

        while (checking in days) {
            count++
            checking = previous(checking)
        }

        _streak.value = count
    }

    private fun previous(key: String): String {
        val (y, m, d) = key.split("-").map { it.toInt() }
        val cal = Calendar.getInstance().apply { set(y, m - 1, d); add(Calendar.DAY_OF_MONTH, -1) }
        return format(cal.time)
    }

    // Factory so viewModel(factory = ForestViewModelFactory(repo)) works
    class Factory(private val repository: SessionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForestViewModel::class.java)) {
                return ForestViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// Helper alias expected by AppNavHost earlier
typealias ForestViewModelFactory = ForestViewModel.Factory
