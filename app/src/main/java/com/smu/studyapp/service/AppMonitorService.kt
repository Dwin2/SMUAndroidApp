package com.smu.studyapp.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.smu.studyapp.MyApplication
import com.smu.studyapp.data.entities.AppSession
import com.smu.studyapp.network.SyncWorker
import com.smu.studyapp.utils.NudgeManager
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

    private var confirmedForegroundPackage: String = ""

    // Active session tracking
    private var currentSessionId: String? = null
    private var currentSessionPackage: String = ""

    // Daily prompt rate-limiting (in-memory to avoid DB races)
    private var lastPromptTimeMs: Long = 0L
    private var todayPromptCount: Int = 0
    private var lastPromptDate: String = ""

    private val repository by lazy { (application as MyApplication).repository }
    private val overlayHost by lazy { PromptOverlayHost(this) }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Connected. Watching for foreground app changes.")
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayHost.hideAll()
    }

    // Cache: does a package have a launcher activity / is it a real app the user can be "in"?
    // Used to filter out IMEs (keyboard), system UI overlays, etc. that would otherwise be
    // misread as the user switching apps while a prompt is on screen.
    private val launchableCache = mutableMapOf<String, Boolean>()

    // Home-screen launchers usually have no LAUNCHER activity of their own, so they need their
    // own lookup. Going home must count as leaving the target app, or re-opening it is invisible.
    private val homePackages: Set<String> by lazy {
        try {
            val home = android.content.Intent(android.content.Intent.ACTION_MAIN)
                .addCategory(android.content.Intent.CATEGORY_HOME)
            packageManager.queryIntentActivities(home, 0)
                .mapNotNull { it.activityInfo?.packageName }
                .toSet()
        } catch (e: Throwable) { emptySet() }
    }

    private fun isUserFacingApp(pkg: String): Boolean {
        if (pkg in homePackages) return true
        return launchableCache.getOrPut(pkg) {
            try {
                packageManager.getLaunchIntentForPackage(pkg) != null
            } catch (e: Throwable) { false }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        // Skip non-launchable packages (IMEs, SystemUI, notification shade, etc.). These
        // fire TYPE_WINDOW_STATE_CHANGED when the keyboard appears or the user pulls down
        // the shade and previously could be misread as "app closed". We also keep
        // accepting our current target app so we don't lose its confirmation in-flight.
        if (pkg != confirmedForegroundPackage && !isUserFacingApp(pkg)) {
            Log.v(TAG, "ignoring non-user-facing pkg=$pkg")
            return
        }

        // A single event from a user-facing app is enough: transient windows (keyboard, shade,
        // our own overlay) are already filtered above. Requiring two events in a row missed
        // launchers and apps that emit only one TYPE_WINDOW_STATE_CHANGED on open.
        Log.d(TAG, "foreground=$pkg")
        handleForegroundChange(pkg)
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
        val participant = repository.getParticipant()
        if (participant == null) {
            Log.w(TAG, "openNewSession($packageName): no participant")
            return
        }
        if (!participant.setupComplete) {
            Log.w(TAG, "openNewSession($packageName): setup not complete")
            return
        }
        if (participant.studyGroup != "T" && participant.studyGroup != "C") {
            Log.w(TAG, "openNewSession($packageName): no allocated condition, skipping")
            return
        }

        val studyDay = SamplingManager.getCurrentStudyDay(participant)
        // studyDay 0 = install/setup day (no prompts yet, study starts tomorrow at window start);
        // studyDay > 7 = study over.
        if (studyDay < 1 || studyDay > 7) {
            Log.w(TAG, "openNewSession($packageName): studyDay=$studyDay outside 1..7, skipping prompt")
            // Still allow session logging below by NOT returning here? Spec says only Day 0
            // is "setup only, no prompts fired" — sessions need not be logged. Skip entirely.
            return
        }

        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        if (today != lastPromptDate) {
            todayPromptCount = 0
            lastPromptDate = today
        }

        val withinWindow = SamplingManager.isWithinSamplingWindow(
            participant.samplingWindowStartMin, participant.samplingWindowEndMin
        )

        val now = System.currentTimeMillis()
        val minutesSinceLast = TimeUnit.MILLISECONDS.toMinutes(now - lastPromptTimeMs)

        val dismissTs = DismissRegistry.lastDismissAt(packageName)
        val msSinceDismiss = now - dismissTs
        val recentlyDismissed = dismissTs > 0 && msSinceDismiss < SamplingManager.PROMPT_REENTRY_WINDOW_MS
        val dismissedTooLongAgo = dismissTs > 0 && msSinceDismiss >= SamplingManager.PROMPT_REENTRY_WINDOW_MS

        // Effective count toward the 15/day cap: open-prompt responses (answered + capped
        // skips) plus the currently-in-flight count of prompts shown today. Close prompts
        // do NOT count. Skips beyond MAX_SKIPS_COUNTED_PER_DAY also do NOT count.
        val skipCount = repository.getSkipCountForDay(studyDay)
        val openResponseCount = repository.getPromptCountForDay(studyDay)
        val answeredOpens = (openResponseCount - skipCount).coerceAtLeast(0)
        val countedSkips = minOf(skipCount, SamplingManager.MAX_SKIPS_COUNTED_PER_DAY)
        val effectiveOpenCount = answeredOpens + countedSkips
        val canPromptByRules = withinWindow
                && effectiveOpenCount < SamplingManager.MAX_PROMPTS_PER_DAY
                && (lastPromptTimeMs == 0L || minutesSinceLast >= SamplingManager.MIN_INTERVAL_MINUTES)

        // Nudge mode: a prior daily-first / consecutive-skip may arm a re-anchoring prompt for
        // this open. A nudge fires even when the normal dismiss-suppression / interval / cap
        // rules would block it — it must still land within the sampling window.
        val (mode, nudgeTrigger) = NudgeManager.decideMode(applicationContext, packageName)
        val nudge = mode == NudgeManager.MODE_NUDGE

        // 1. Nudge armed → fire (overrides dismiss suppression) as long as we're in-window.
        // 2. Re-entry within 1 min → re-show.
        // 3. Re-entry after 1 min → suppress prompt entirely (still records the session).
        // 4. Otherwise apply normal rules.
        val shouldPrompt = when {
            nudge -> withinWindow
            recentlyDismissed -> true
            dismissedTooLongAgo -> false
            else -> canPromptByRules
        }

        // Reuse the previous session ID when re-showing within the re-entry window so the
        // dashboard groups the open prompt and the closing satisfaction together. A nudge is a
        // fresh session (it fires on a later re-open, not a quick dismiss-and-return).
        val sessionId = if (recentlyDismissed && !nudge) {
            DismissRegistry.lastSessionId(packageName) ?: UUID.randomUUID().toString()
        } else UUID.randomUUID().toString()

        val session = AppSession(
            sessionId = sessionId,
            participantCode = participant.participantCode,
            appPackage = packageName,
            appName = SamplingManager.getAppName(packageName),
            openTime = now,
            studyDay = studyDay,
            promptShown = shouldPrompt,
            promptType = if (shouldPrompt) participant.studyGroup else "",
            withinSamplingWindow = withinWindow
        )
        repository.saveSession(session)

        currentSessionId = sessionId
        currentSessionPackage = packageName

        Log.i(TAG, "openNewSession pkg=$packageName shouldPrompt=$shouldPrompt withinWindow=$withinWindow effectiveCount=$effectiveOpenCount skips=$skipCount minSinceLast=$minutesSinceLast recentlyDismissed=$recentlyDismissed mode=$mode trigger=$nudgeTrigger")

        if (shouldPrompt) {
            // Don't double-count when re-entering after dismiss
            if (!recentlyDismissed) {
                lastPromptTimeMs = now
                todayPromptCount++
            }
            if (nudge) {
                // Showing the nudge resets the counters; normal Standard-mode behavior resumes.
                NudgeManager.onNudgeShown(applicationContext, nudgeTrigger)
            }
            showOpenPrompt(sessionId, packageName, participant.studyGroup, mode, nudgeTrigger, studyDay)
        }

        if (dismissedTooLongAgo) {
            DismissRegistry.clear(packageName)
        }
    }

    private fun closeCurrentSession(packageName: String) {
        val sessionId = currentSessionId ?: return
        currentSessionId = null
        currentSessionPackage = ""

        scope.launch {
            val session = repository.getSession(sessionId) ?: return@launch
            val now = System.currentTimeMillis()
            // Reset synced=false so the close time re-uploads. The open event may have
            // already synced this row with closeTime=0 (via the open-prompt answer or the
            // periodic worker); without this the session duration never reaches the server.
            repository.updateSession(session.copy(closeTime = now, synced = false))

            val durationMs = now - session.openTime
            val dismissTs = DismissRegistry.lastDismissAt(packageName)
            val recentlyDismissed = dismissTs > 0 &&
                now - dismissTs < SamplingManager.PROMPT_REENTRY_WINDOW_MS

            // Per spec, fire the closing satisfaction only if ALL hold:
            //   1. an opening prompt fired
            //   2. user wasn't actively dismissing-and-re-entering
            //   3. session was a "real" session (>= 2s of foreground time)
            //   4. user actually typed something meaningful on the open prompt (>= 3 chars)
            val openingResponse = repository.getOpeningResponseForSession(sessionId)
            val answerLen = extractAnswerLength(openingResponse)
            val isRealSession = durationMs >= SamplingManager.MIN_REAL_SESSION_MS
            val openAnswered = answerLen >= SamplingManager.MIN_OPEN_ANSWER_CHARS

            if (session.promptShown && !recentlyDismissed && isRealSession && openAnswered) {
                showSatisfactionPrompt(sessionId, packageName)
            } else {
                Log.i(
                    TAG,
                    "skip satisfaction: shown=${session.promptShown} dismissed=$recentlyDismissed " +
                        "real=$isRealSession (${durationMs}ms) answerLen=$answerLen"
                )
            }
            SyncWorker.triggerNow(applicationContext)
        }
    }

    /** Pulls the user's typed answer length out of the stored MRP/NP response. 0 if skipped/missing. */
    private fun extractAnswerLength(response: com.smu.studyapp.data.entities.SurveyResponse?): Int {
        if (response == null) return 0
        return try {
            val map = com.google.gson.Gson().fromJson(response.responseJson, Map::class.java)
            if (map?.get("skipped") == true) return 0
            val text = (map?.get("motivation") ?: map?.get("answer") ?: "").toString()
            text.trim().length
        } catch (e: Exception) { 0 }
    }

    private fun showOpenPrompt(
        sessionId: String,
        packageName: String,
        studyGroup: String,
        mode: String,
        nudgeTrigger: String,
        studyDay: Int
    ) {
        overlayHost.show(
            sessionId = sessionId,
            packageName = packageName,
            promptType = studyGroup, // "T" or "C"
            mode = mode,
            nudgeTrigger = nudgeTrigger,
            studyDay = studyDay,
            onSkipped = { _, pkg, type, m, tr -> handleSkip(pkg, sessionId, type, m, tr) }
        )
    }

    private fun showSatisfactionPrompt(sessionId: String, packageName: String) {
        overlayHost.show(
            sessionId = sessionId,
            packageName = packageName,
            promptType = "SATISFACTION",
            onSkipped = { _, pkg, type, m, tr -> handleSkip(pkg, sessionId, type, m, tr) }
        )
    }

    /**
     * Skipping an open prompt. Behavior depends on the prompt's mode:
     *
     *  - Standard mode: arms the 1-minute re-entry window and sends the user home (closes the
     *    target app). Bumps the nudge counters (consecutive skip + daily-first arm) so the next
     *    qualifying open re-fires in nudge mode.
     *  - Nudge mode ("Skip for now"): dismisses the prompt and lets the social-media app open
     *    normally (no app close, no re-entry arming). Counters were already reset when the nudge
     *    was shown.
     *
     * Either way the skip is logged (survey response with "skipped": true, plus mode/nudge_trigger)
     * so it shows up on the dashboard and in the export. SATISFACTION is not skippable.
     */
    private fun handleSkip(
        packageName: String,
        sessionId: String,
        promptType: String,
        mode: String,
        nudgeTrigger: String
    ) {
        if (promptType == "T" || promptType == "C") {
            scope.launch {
                com.smu.studyapp.ui.prompts.PromptSkipLogger.logSkip(
                    repository, sessionId, packageName, promptType, mode, nudgeTrigger
                )
                com.smu.studyapp.network.SyncWorker.triggerNow(applicationContext)
            }
            if (mode == NudgeManager.MODE_NUDGE) {
                // "Skip for now" — let the app open as usual; don't close or arm re-entry.
                return
            }
            NudgeManager.onStandardSkip(applicationContext, packageName)
            DismissRegistry.recordDismiss(packageName, sessionId)
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }
}

/** In-memory dismiss tracker keyed by package name. */
object DismissRegistry {
    private val lastDismissTime = mutableMapOf<String, Long>()
    private val lastSession = mutableMapOf<String, String>()

    @Synchronized
    fun recordDismiss(pkg: String, sessionId: String) {
        lastDismissTime[pkg] = System.currentTimeMillis()
        lastSession[pkg] = sessionId
    }

    @Synchronized
    fun lastDismissAt(pkg: String): Long = lastDismissTime[pkg] ?: 0L

    @Synchronized
    fun lastSessionId(pkg: String): String? = lastSession[pkg]

    @Synchronized
    fun clear(pkg: String) {
        lastDismissTime.remove(pkg)
        lastSession.remove(pkg)
    }
}
