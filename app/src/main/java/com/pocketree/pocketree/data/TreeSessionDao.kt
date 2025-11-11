package com.pocketree.pocketree.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TreeSessionDao {

    @Insert
    suspend fun insertSession(session: TreeSession)

    @Query("SELECT * FROM tree_sessions ORDER BY timestamp DESC")
    suspend fun getAllSessions(): List<TreeSession>
}
