package com.pocketree.pocketree.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pocketree.pocketree.data.model.TreeSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TreeSession)

    @Query("SELECT * FROM tree_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TreeSession>>

    @Query("SELECT * FROM tree_sessions WHERE isWithered = 0 ORDER BY startTime DESC")
    fun getSuccessfulSessions(): Flow<List<TreeSession>>

    @Query("SELECT * FROM tree_sessions WHERE isWithered = 1 ORDER BY startTime DESC")
    fun getFailedSessions(): Flow<List<TreeSession>>
}
