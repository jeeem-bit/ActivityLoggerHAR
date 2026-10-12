package com.example.activityloggerhar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class RecordingService : Service() {
    private var logger: SensorLogger? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (logger == null) {
                    startForegroundNotification()
                    acquireWakeLock()
                    logger = SensorLogger(applicationContext) { currentLabel }
                        .also { it.start() }
                    isRecording = true
                }
            }
            ACTION_STOP -> {
                logger?.stop()
                logger = null
                isRecording = false
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "recording"
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Recording",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Recording sensor data")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(1, notification)
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "har:recording"
        ).apply {
            acquire(30 * 60 * 1000L) // safety timeout: 30 min
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    override fun onDestroy() {
        logger?.stop()
        logger = null
        isRecording = false
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
        @Volatile var currentLabel: String = "None"
        @Volatile var isRecording: Boolean = false
    }
}