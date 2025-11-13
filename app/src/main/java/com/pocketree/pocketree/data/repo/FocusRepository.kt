package com.pocketree.pocketree.data.repo

import com.pocketree.pocketree.data.db.FocusSessionDao
import com.pocketree.pocketree.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

class FocusRepository(private val dao: FocusSessionDao) {
    suspend fun addSession(session: FocusSession) {
        dao.insertSession(session)
    }

    fun getAllSessions(): Flow<List<FocusSession>> = dao.getAllSessions()

    fun countCompleted(): Flow<Int> = dao.countCompleted()
}
