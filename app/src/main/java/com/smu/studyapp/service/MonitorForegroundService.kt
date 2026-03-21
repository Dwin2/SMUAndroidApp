package com.smu.studyapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smu.studyapp.utils.NotificationHelper

/**
 * Minimal foreground service that keeps the process alive for the accessibility service.
 */
class MonitorForegroundService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notif = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_MONITOR)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SMU Study Active")
            .setContentText("Monitoring social media app usage for the study.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
        startForeground(999, notif)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
