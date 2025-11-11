package com.pocketree.pocketree

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import com.pocketree.pocketree.data.AppDatabase
import com.pocketree.pocketree.data.AppLifecycleObserver

class PockeTreeApp : Application() {

    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Room database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "pocketree-db"
        ).build()

        // Add lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }
}
