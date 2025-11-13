package com.pocketree.pocketree.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pocketree.pocketree.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insertSession(session: FocusSession)

    // Correct table name = FocusSession
    @Query("SELECT * FROM FocusSession ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    // If you use this
    @Query("SELECT COUNT(*) FROM FocusSession WHERE completed = 1")
    fun countCompleted(): Flow<Int>
}
