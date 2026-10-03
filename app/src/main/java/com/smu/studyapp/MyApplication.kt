package com.smu.studyapp

import android.app.Application
import com.smu.studyapp.data.AppDatabase
import com.smu.studyapp.data.repository.StudyRepository
import com.smu.studyapp.network.NetworkModule
import com.smu.studyapp.network.SyncWorker
import com.smu.studyapp.utils.NotificationHelper

class MyApplication : Application() {
    val repository: StudyRepository by lazy {
        StudyRepository(AppDatabase.getDatabase(this))
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        if (NetworkModule.isConfigured) {
            SyncWorker.schedulePeriodic(this)
        }
    }
}
