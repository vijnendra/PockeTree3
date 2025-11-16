package com.pocketree.pocketree

import android.app.Application
import com.pocketree.pocketree.data.db.AppDatabase
import com.pocketree.pocketree.data.repository.SessionRepository

class PockeTreeApp : Application() {

    lateinit var repository: SessionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        val db = AppDatabase.getInstance(this)
        repository = SessionRepository(db.focusSessionDao())
    }

    companion object {
        lateinit var instance: PockeTreeApp
            private set
    }
}
