package com.smu.studyapp.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.smu.studyapp.utils.NotificationHelper
import java.util.Calendar

object EMAScheduler {

    const val SURVEY_EXPIRY_MS = 3L * 60 * 60 * 1000  // 3 hours
    const val FOLLOWUP_DELAY_MS = 60L * 60 * 1000     // 1 hour

    // AlarmManager request codes (must be stable across app launches for cancellation)
    private const val REQ_5PM_INITIAL = 2001
    private const val REQ_9PM_INITIAL = 2002
    private const val REQ_5PM_FOLLOWUP = 2101
    private const val REQ_9PM_FOLLOWUP = 2102
    private const val REQ_5PM_EXPIRY = 2201
    private const val REQ_9PM_EXPIRY = 2202

    const val EVENT_INITIAL = "initial"
    const val EVENT_FOLLOWUP = "followup"
    const val EVENT_EXPIRY = "expiry"

    fun scheduleDaily(context: Context, notBeforeMs: Long = 0L) {
        scheduleInitial(context, 17, 0, "EMA_5PM", REQ_5PM_INITIAL, notBeforeMs)
        scheduleInitial(context, 21, 0, "EMA_9PM", REQ_9PM_INITIAL, notBeforeMs)
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(
            REQ_5PM_INITIAL, REQ_9PM_INITIAL,
            REQ_5PM_FOLLOWUP, REQ_9PM_FOLLOWUP,
            REQ_5PM_EXPIRY, REQ_9PM_EXPIRY
        ).forEach { reqCode ->
            val intent = Intent(context, EMAAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, reqCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }
    }

    private fun scheduleInitial(
        context: Context,
        hour: Int,
        minute: Int,
        surveyType: String,
        reqCode: Int,
        notBeforeMs: Long = 0L
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EMAAlarmReceiver::class.java).apply {
            putExtra("survey_type", surveyType)
            putExtra("event", EVENT_INITIAL)
        }
        val pi = PendingIntent.getBroadcast(
            context, reqCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Advance until both: not in the past AND not before study Day 1 starts.
            // This is what makes "install day = setup only" actually work — the next 5pm
            // or 9pm following the participant's chosen window-start is when EMAs begin.
            val floor = maxOf(System.currentTimeMillis(), notBeforeMs)
            while (timeInMillis <= floor) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        am.setRepeating(
            AlarmManager.RTC_WAKEUP, cal.timeInMillis,
            AlarmManager.INTERVAL_DAY, pi
        )
    }

    /** Called from EMAAlarmReceiver after the initial alarm fires, to schedule follow-up + expiry. */
    fun scheduleFollowUpAndExpiry(context: Context, surveyType: String, triggerTimeMs: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val (followupReq, expiryReq) = when (surveyType) {
            "EMA_5PM" -> REQ_5PM_FOLLOWUP to REQ_5PM_EXPIRY
            else -> REQ_9PM_FOLLOWUP to REQ_9PM_EXPIRY
        }

        val followupIntent = Intent(context, EMAAlarmReceiver::class.java).apply {
            putExtra("survey_type", surveyType)
            putExtra("event", EVENT_FOLLOWUP)
            putExtra("trigger_time_ms", triggerTimeMs)
        }
        val followupPi = PendingIntent.getBroadcast(
            context, followupReq, followupIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMs + FOLLOWUP_DELAY_MS,
            followupPi
        )

        val expiryIntent = Intent(context, EMAAlarmReceiver::class.java).apply {
            putExtra("survey_type", surveyType)
            putExtra("event", EVENT_EXPIRY)
        }
        val expiryPi = PendingIntent.getBroadcast(
            context, expiryReq, expiryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMs + SURVEY_EXPIRY_MS,
            expiryPi
        )
    }
}
