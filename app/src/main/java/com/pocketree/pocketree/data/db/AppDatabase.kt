package com.pocketree.pocketree.data

import androidx.room.Database
import androidx.room.RoomDatabase

// Replace YourEntity with your actual entity classes
@Database(entities = [YourEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun yourDao(): YourDao
}
