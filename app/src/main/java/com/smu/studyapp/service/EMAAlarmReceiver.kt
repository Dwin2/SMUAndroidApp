package com.smu.studyapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smu.studyapp.MyApplication
import com.smu.studyapp.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class EMAAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val surveyType = intent.getStringExtra("survey_type") ?: return
        val event = intent.getStringExtra("event") ?: EMAScheduler.EVENT_INITIAL
        val notifId = if (surveyType == "EMA_5PM") NotificationHelper.NOTIF_5PM else NotificationHelper.NOTIF_9PM

        when (event) {
            EMAScheduler.EVENT_INITIAL -> {
                val triggerTime = System.currentTimeMillis()
                NotificationHelper.showEMANotification(
                    context = context,
                    surveyType = surveyType,
                    notifId = notifId,
                    triggerTimeMs = triggerTime,
                    isFollowUp = false
                )
                EMAScheduler.scheduleFollowUpAndExpiry(context, surveyType, triggerTime)
            }
            EMAScheduler.EVENT_FOLLOWUP -> {
                val triggerTime = intent.getLongExtra("trigger_time_ms", System.currentTimeMillis())
                if (!hasCompleted(context, surveyType, triggerTime)) {
                    NotificationHelper.showEMANotification(
                        context = context,
                        surveyType = surveyType,
                        notifId = notifId,
                        triggerTimeMs = triggerTime,
                        isFollowUp = true
                    )
                }
            }
            EMAScheduler.EVENT_EXPIRY -> {
                NotificationHelper.cancelEMANotification(context, notifId)
            }
        }
    }

    /** True if the participant submitted the survey at any point since it was triggered. */
    private fun hasCompleted(context: Context, surveyType: String, triggerTimeMs: Long): Boolean {
        val app = context.applicationContext as MyApplication
        // Repository methods are suspend; this receiver runs on the main thread, so block briefly.
        return runBlocking(Dispatchers.IO) {
            val responses = app.repository
                .surveysSinceTimestamp(surveyType, triggerTimeMs)
            responses > 0
        }
    }
}
