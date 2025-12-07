package com.pocketree.pocketree

import android.app.Application
import com.pocketree.pocketree.data.repository.TreeSessionRepository

class PockeTreeApp : Application() {

    lateinit var treeSessionRepository: TreeSessionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        treeSessionRepository = TreeSessionRepository(this)
    }

    companion object {
        lateinit var instance: PockeTreeApp
            private set
    }
}
