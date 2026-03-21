package com.smu.studyapp.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object EMAScheduler {

    private const val REQ_5PM = 2001
    private const val REQ_9PM = 2002

    fun scheduleDaily(context: Context) {
        schedule(context, 17, 0, "EMA_5PM", REQ_5PM)
        schedule(context, 21, 0, "EMA_9PM", REQ_9PM)
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(REQ_5PM to "EMA_5PM", REQ_9PM to "EMA_9PM").forEach { (reqCode, type) ->
            val intent = Intent(context, EMAAlarmReceiver::class.java).apply {
                putExtra("survey_type", type)
            }
            val pi = PendingIntent.getBroadcast(context, reqCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            am.cancel(pi)
        }
    }

    private fun schedule(context: Context, hour: Int, minute: Int, surveyType: String, reqCode: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EMAAlarmReceiver::class.java).apply {
            putExtra("survey_type", surveyType)
        }
        val pi = PendingIntent.getBroadcast(context, reqCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            // If already past today's time, start tomorrow
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis,
            AlarmManager.INTERVAL_DAY, pi)
    }
}
