package com.smu.studyapp.network

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.smu.studyapp.MyApplication
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!NetworkModule.isConfigured) {
            Log.w(TAG, "Backend not configured; skipping")
            return Result.success()
        }
        val app = applicationContext as MyApplication
        val syncManager = SyncManager(app.repository)
        return when (val r = syncManager.syncNow()) {
            is SyncManager.Result.Ok -> {
                Log.i(TAG, "Sync ok: ${r.surveys} surveys, ${r.sessions} sessions")
                Result.success()
            }
            is SyncManager.Result.Skipped -> {
                Log.i(TAG, "Sync skipped: ${r.reason}")
                Result.success()
            }
            is SyncManager.Result.Failed -> {
                Log.w(TAG, "Sync failed: ${r.message} (will retry)")
                if (runAttemptCount < 5) Result.retry() else Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val PERIODIC_NAME = "smu-sync-periodic"
        private const val ONESHOT_NAME = "smu-sync-oneshot"

        private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun schedulePeriodic(context: Context) {
            val req = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                req
            )
        }

        fun triggerNow(context: Context) {
            val req = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.SECONDS
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONESHOT_NAME,
                ExistingWorkPolicy.REPLACE,
                req
            )
        }
    }
}
