package com.pocketree.pocketree.data.repository

import com.pocketree.pocketree.data.db.FocusSessionDao
import com.pocketree.pocketree.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

class SessionRepository(
    private val dao: FocusSessionDao
) {

    fun getAllSessions(): Flow<List<FocusSession>> =
        dao.getAllSessions()

    suspend fun getAllSessionsOnce(): List<FocusSession> =
        dao.getAllSessionsOnce()

    suspend fun getSessionsForDay(dayKey: String): List<FocusSession> =
        dao.getSessionsForDay(dayKey)

    suspend fun getUniqueDays(): List<String> =
        dao.getUniqueDays()

    suspend fun insertSession(durationMinutes: Int, wasWithered: Boolean) {
        dao.insertSession(
            FocusSession(
                durationMinutes = durationMinutes,
                wasWithered = wasWithered
            )
        )
    }
}
