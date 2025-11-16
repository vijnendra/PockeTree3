package com.pocketree.pocketree.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pocketree.pocketree.data.model.TreeSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeSessionDao {

    @Insert
    suspend fun insertSession(session: TreeSession)

    @Query("SELECT * FROM tree_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TreeSession>>

    @Query("SELECT * FROM tree_sessions WHERE startTime BETWEEN :start AND :end")
    fun getSessionsInRange(start: Long, end: Long): Flow<List<TreeSession>>
}
