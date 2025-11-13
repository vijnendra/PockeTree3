package com.pocketree.pocketree

import android.app.Application
import androidx.room.Room
import com.pocketree.pocketree.data.db.AppDatabase

class PockeTreeApp : Application() {
    companion object {
        lateinit var db: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "pocketree.db"
        ).fallbackToDestructiveMigration()
            .build()
    }
}
