package com.pocketree.pocketree.ui.forest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pocketree.pocketree.data.model.TreeSession
import com.pocketree.pocketree.data.repository.TreeSessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ForestViewModel(
    private val repository: TreeSessionRepository
) : ViewModel() {

    private val _allSessions = MutableStateFlow<List<TreeSession>>(emptyList())

    private val _displaySessions = MutableStateFlow<List<TreeSession>>(emptyList())
    val displaySessions: StateFlow<List<TreeSession>> = _displaySessions

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    init {
        viewModelScope.launch {
            repository.getAll()
                .collect { list ->
                    _allSessions.value = list.sortedByDescending { it.startTime }
                    setTab(0) // default = Day
                    calculateStreak()
                }
        }
    }

    fun setTab(tab: Int) {
        when (tab) {
            0 -> filterDay()
            1 -> filterMonth()
            2 -> filterYear()
            3 -> filterLifetime()
        }
    }

    private fun filterDay() {
        _displaySessions.value = _allSessions.value.filter { isSameDay(it.startTime) }
    }

    private fun filterMonth() {
        _displaySessions.value = _allSessions.value.filter { isSameMonth(it.startTime) }
    }

    private fun filterYear() {
        _displaySessions.value = _allSessions.value.filter { isSameYear(it.startTime) }
    }

    private fun filterLifetime() {
        _displaySessions.value = _allSessions.value
    }

    private fun isSameDay(time: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = time }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameMonth(time: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = time }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    private fun isSameYear(time: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = time }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    private fun calculateStreak() {
        val doneDays = _allSessions.value
            .filter { !it.isWithered && it.durationMinutes > 0 } // successful only
            .map { dayKey(it.startTime) }
            .toSet()

        var count = 0
        var checking = dayKey(System.currentTimeMillis())

        while (checking in doneDays) {
            count++
            checking = previousDay(checking)
        }

        _streak.value = count
    }

    private fun dayKey(time: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun previousDay(key: String): String {
        val (y, m, d) = key.split("-").map { it.toInt() }
        return dayKey(
            Calendar.getInstance().apply {
                set(y, m - 1, d)
                add(Calendar.DAY_OF_YEAR, -1)
            }.timeInMillis
        )
    }

    class Factory(private val repo: TreeSessionRepository) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ForestViewModel(repo) as T
        }
    }
}

typealias ForestViewModelFactory = ForestViewModel.Factory
