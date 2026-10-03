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

    // Notification IDs (also reused by AlarmManager request codes for cancellation)
    const val NOTIF_5PM = 1001
    const val NOTIF_9PM = 1002

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

    fun showEMANotification(
        context: Context,
        surveyType: String,
        notifId: Int,
        triggerTimeMs: Long,
        isFollowUp: Boolean = false
    ) {
        val title = "Time for your quick check-in!"
        val text = if (isFollowUp) {
            "Just a reminder — your study check-in is still waiting. It takes about a minute and closes soon!"
        } else {
            "It only takes a minute! Open the app to share how your day is going."
        }

        val intent = Intent(context, EMAActivity::class.java).apply {
            putExtra("survey_type", surveyType)
            putExtra("trigger_time_ms", triggerTimeMs)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_EMA)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(notifId, notif)
    }

    fun cancelEMANotification(context: Context, notifId: Int) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(notifId)
    }
}
