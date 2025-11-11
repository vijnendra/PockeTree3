package com.pocketree.pocketree.service

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

object AppLifecycleObserver : DefaultLifecycleObserver {

    interface Listener {
        fun onBackgrounded() {}
        fun onForegrounded() {}
    }

    private val listeners = mutableSetOf<Listener>()

    fun register(l: Listener) { listeners.add(l) }
    fun unregister(l: Listener) { listeners.remove(l) }

    override fun onStart(owner: LifecycleOwner) {
        listeners.forEach { it.onForegrounded() }
    }

    override fun onStop(owner: LifecycleOwner) {
        listeners.forEach { it.onBackgrounded() }
    }
}
