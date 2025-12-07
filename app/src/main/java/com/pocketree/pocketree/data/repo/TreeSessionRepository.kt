package com.pocketree.pocketree.data.repository

import android.content.Context
import com.pocketree.pocketree.data.db.AppDatabase
import com.pocketree.pocketree.data.model.TreeSession

class TreeSessionRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).treeSessionDao()

    suspend fun insert(session: TreeSession) = dao.insertSession(session)

    fun getAll() = dao.getAllSessions()
    fun getSuccess() = dao.getSuccessfulSessions()
    fun getFailed() = dao.getFailedSessions()
}
