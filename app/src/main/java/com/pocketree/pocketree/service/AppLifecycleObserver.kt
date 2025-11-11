package com.pocketree.pocketree.data

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class AppLifecycleObserver : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        // App enters foreground
        println("App in foreground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        // App enters background
        println("App in background")
    }
}
