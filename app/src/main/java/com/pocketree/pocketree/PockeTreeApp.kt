package com.pocketree.pocketree

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import com.pocketree.pocketree.data.db.AppDatabase
import com.pocketree.pocketree.service.AppLifecycleObserver   // ← this must resolve

class PockeTreeApp : Application() {
    companion object {
        lateinit var db: AppDatabase
            private set
    }
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDatabase::class.java, "pocketree.db").build()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver)
    }
}
