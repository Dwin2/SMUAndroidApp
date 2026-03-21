package com.smu.studyapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smu.studyapp.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        CoroutineScope(Dispatchers.IO).launch {
            val app = context.applicationContext as MyApplication
            val participant = app.repository.getParticipant()
            if (participant?.setupComplete == true) {
                EMAScheduler.scheduleDaily(context)
            }
        }
    }
}
