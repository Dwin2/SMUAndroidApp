package com.smu.studyapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.smu.studyapp.ui.ema.EMAActivity

object NotificationHelper {
    const val CHANNEL_EMA = "ema_surveys"
    const val CHANNEL_MONITOR = "monitor_service"

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_EMA, "Survey Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for scheduled study surveys"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_MONITOR, "Study Monitor", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Background service keeping the study running"
            }
        )
    }

    fun showEMANotification(context: Context, surveyType: String, notifId: Int) {
        val label = if (surveyType == "EMA_5PM") "5 PM" else "9 PM"
        val intent = Intent(context, EMAActivity::class.java).apply {
            putExtra("survey_type", surveyType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(context, CHANNEL_EMA)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SMU Study - $label Survey")
            .setContentText("Please complete your daily well-being check-in.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(notifId, notif)
    }
}
