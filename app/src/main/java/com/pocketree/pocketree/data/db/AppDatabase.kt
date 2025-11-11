package com.pocketree.pocketree.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pocketree.pocketree.data.model.FocusSession

@Database(
    entities = [FocusSession::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
}
