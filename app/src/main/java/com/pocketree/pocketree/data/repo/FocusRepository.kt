package com.pocketree.pocketree.data.repo

import com.pocketree.pocketree.data.db.FocusSessionDao
import com.pocketree.pocketree.data.model.FocusSession

class FocusRepository(private val dao: FocusSessionDao) {
    suspend fun save(session: FocusSession) = dao.insert(session)
    fun sessions() = dao.getAllSessions()
    fun completedCount() = dao.countCompleted()
}
