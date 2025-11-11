package com.pocketree.pocketree.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TreeSession::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun treeSessionDao(): TreeSessionDao
}

