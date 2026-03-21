package com.smu.studyapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smu.studyapp.utils.NotificationHelper

class EMAAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val surveyType = intent.getStringExtra("survey_type") ?: return
        val notifId = if (surveyType == "EMA_5PM") 1001 else 1002
        NotificationHelper.showEMANotification(context, surveyType, notifId)
    }
}
