package com.pocketree.pocketree.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pocketree.pocketree.PockeTreeApp
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class FocusService : Service() {

    companion object {
        const val CHANNEL_ID = "FocusServiceChannel"
        const val NOTIFICATION_ID = 101
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var sessionStartMs = 0L
    private var wasWithered = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {

            "START_FOCUS" -> {
                sessionStartMs = System.currentTimeMillis()
                wasWithered = false
                startForeground(NOTIFICATION_ID, buildNotification("Focus Started"))
            }

            "WITHER_TREE" -> {
                wasWithered = true
                stopSelf()
            }

            "STOP_FOCUS" -> {
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        val now = System.currentTimeMillis()
        val elapsedSec = TimeUnit.MILLISECONDS.toSeconds(now - sessionStartMs).toInt()

        // Save session safely inside coroutine
        serviceScope.launch {
            val repository = PockeTreeApp.instance.repository

            repository.insertSession(
                durationMinutes = (elapsedSec / 60),
                wasWithered = wasWithered
            )
        }

        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pocketree Focus")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
