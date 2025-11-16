package com.pocketree.pocketree.data.db

import androidx.room.*
import com.pocketree.pocketree.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insertSession(session: FocusSession)

    @Query("SELECT * FROM focus_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions ORDER BY id DESC")
    suspend fun getAllSessionsOnce(): List<FocusSession>

    @Query("SELECT * FROM focus_sessions WHERE dayKey = :day ORDER BY id DESC")
    suspend fun getSessionsForDay(day: String): List<FocusSession>

    @Query("SELECT DISTINCT dayKey FROM focus_sessions ORDER BY dayKey DESC")
    suspend fun getUniqueDays(): List<String>
}
