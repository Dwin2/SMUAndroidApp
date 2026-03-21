package com.smu.studyapp.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.smu.studyapp.MyApplication
import com.smu.studyapp.data.entities.AppSession
import com.smu.studyapp.ui.prompts.PromptActivity
import com.smu.studyapp.utils.SamplingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

class AppMonitorService : AccessibilityService() {

    private val TAG = "AppMonitorService"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Two-in-a-row detection
    private var lastSeenPackage: String = ""
    private var confirmedForegroundPackage: String = ""

    // Active session tracking
    private var currentSessionId: String? = null
    private var currentSessionPackage: String = ""

    // In-memory prompt tracking — avoids DB race conditions
    private var lastPromptTimeMs: Long = 0L
    private var todayPromptCount: Int = 0
    private var lastPromptDate: String = ""

    private val repository by lazy { (application as MyApplication).repository }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        if (pkg == lastSeenPackage) {
            handleForegroundChange(pkg)
        }
        lastSeenPackage = pkg
    }

    private fun handleForegroundChange(newPackage: String) {
        if (newPackage == confirmedForegroundPackage) return
        val prevPackage = confirmedForegroundPackage
        confirmedForegroundPackage = newPackage

        if (SamplingManager.isTargetApp(prevPackage) && currentSessionPackage == prevPackage) {
            closeCurrentSession(prevPackage)
        }

        if (SamplingManager.isTargetApp(newPackage)) {
            scope.launch {
                val participant = repository.getParticipant() ?: return@launch
                if (SamplingManager.isSelectedApp(newPackage, participant.selectedApps)) {
                    openNewSession(newPackage)
                }
            }
        }
    }

    private suspend fun openNewSession(packageName: String) {
        val participant = repository.getParticipant() ?: return
        if (!participant.setupComplete) return

        val studyDay = SamplingManager.getCurrentStudyDay(participant.enrollmentDate)
        if (studyDay > 7) return

        // Reset daily counter if it's a new day
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        if (today != lastPromptDate) {
            todayPromptCount = 0
            lastPromptDate = today
        }

        val withinWindow = SamplingManager.isWithinSamplingWindow(
            participant.samplingWindowStart, participant.samplingWindowEnd
        )

        val minutesSinceLast = TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - lastPromptTimeMs
        )
        val canPrompt = withinWindow
                && todayPromptCount < SamplingManager.MAX_PROMPTS_PER_DAY
                && (lastPromptTimeMs == 0L || minutesSinceLast >= SamplingManager.MIN_INTERVAL_MINUTES)

        val sessionId = UUID.randomUUID().toString()
        val session = AppSession(
            sessionId = sessionId,
            participantCode = participant.participantCode,
            appPackage = packageName,
            appName = SamplingManager.getAppName(packageName),
            openTime = System.currentTimeMillis(),
            studyDay = studyDay,
            promptShown = canPrompt,
            promptType = if (canPrompt) participant.studyGroup else "",
            withinSamplingWindow = withinWindow
        )
        repository.saveSession(session)

        currentSessionId = sessionId
        currentSessionPackage = packageName

        if (canPrompt) {
            // Update in-memory counters immediately
            lastPromptTimeMs = System.currentTimeMillis()
            todayPromptCount++
            launchPromptActivity(sessionId, packageName, participant.studyGroup)
        }
    }

    private fun closeCurrentSession(packageName: String) {
        val sessionId = currentSessionId ?: return
        currentSessionId = null
        currentSessionPackage = ""

        scope.launch {
            val session = repository.getSession(sessionId) ?: return@launch
            repository.updateSession(session.copy(closeTime = System.currentTimeMillis()))
            // Only show satisfaction if an opening prompt was shown this session (per spec)
            if (session.promptShown) {
                launchSatisfactionActivity(sessionId, packageName)
            }
        }
    }

    private fun launchPromptActivity(sessionId: String, packageName: String, studyGroup: String) {
        startActivity(Intent(this, PromptActivity::class.java).apply {
            putExtra(PromptActivity.EXTRA_SESSION_ID, sessionId)
            putExtra(PromptActivity.EXTRA_APP_PACKAGE, packageName)
            putExtra(PromptActivity.EXTRA_PROMPT_TYPE, studyGroup)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
    }

    private fun launchSatisfactionActivity(sessionId: String, packageName: String) {
        startActivity(Intent(this, PromptActivity::class.java).apply {
            putExtra(PromptActivity.EXTRA_SESSION_ID, sessionId)
            putExtra(PromptActivity.EXTRA_APP_PACKAGE, packageName)
            putExtra(PromptActivity.EXTRA_PROMPT_TYPE, "SATISFACTION")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }
}
