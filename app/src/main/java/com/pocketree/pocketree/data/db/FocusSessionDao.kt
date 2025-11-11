package com.pocketree.pocketree.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.pocketree.pocketree.data.model.FocusSession

@Dao
interface FocusSessionDao {
    @Insert suspend fun insert(session: FocusSession): Long

    @Query("SELECT * FROM focus_sessions ORDER BY startTs DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1")
    fun countCompleted(): Flow<Int>
}
