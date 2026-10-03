package com.smu.studyapp.utils

import com.smu.studyapp.BuildConfig
import com.smu.studyapp.data.repository.StudyRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit

object SamplingManager {

    // TESTING: frequency caps relaxed so prompts fire on essentially every qualifying app
    // open (up to 30/day, no minimum spacing). Revert to 15 / 5 / 60L for production.
    const val MAX_PROMPTS_PER_DAY = 30
    const val MAX_SKIPS_COUNTED_PER_DAY = 100    // skips beyond this don't consume a slot
    const val MIN_INTERVAL_MINUTES = 0L
    const val PROMPT_REENTRY_WINDOW_MS = 60_000L  // re-show prompt if same app reopens within 1 min of dismiss
    const val MIN_REAL_SESSION_MS = 2_000L       // <2s of foreground time → no closing satisfaction prompt
    const val MIN_OPEN_ANSWER_CHARS = 3          // open prompt answers shorter than this don't trigger close survey

    // Full catalog the study cares about. Apps are only actually tracked when installed
    // on the participant's device (see installedTargetPackages() below).
    private val PRODUCTION_NAMES = linkedMapOf(
        // Video-based social media
        "com.zhiliaoapp.musically" to "TikTok",
        "com.ss.android.ugc.trill" to "TikTok",          // global build
        "com.instagram.android" to "Instagram",
        "com.google.android.youtube" to "YouTube",
        "com.snapchat.android" to "Snapchat",
        "tv.twitch.android.app" to "Twitch",
        "com.bereal.ft" to "BeReal",
        // Text / photo / mixed social media
        "com.facebook.katana" to "Facebook",
        "com.twitter.android" to "X (Twitter)",
        "com.zhiliaoapp.threads" to "Threads",
        "com.instagram.threadsapp" to "Threads",
        "com.reddit.frontpage" to "Reddit",
        "com.bluesky.app" to "Bluesky",                  // tentative
        "bsky.app" to "Bluesky",
        "org.joinmastodon.android" to "Mastodon",
        "com.tumblr" to "Tumblr",
        "com.pinterest" to "Pinterest",
        "com.linkedin.android" to "LinkedIn",
        "com.discord" to "Discord",
        "com.quora.android" to "Quora",
        // Messaging
        "com.whatsapp" to "WhatsApp",
        "org.telegram.messenger" to "Telegram",
        "com.facebook.orca" to "Messenger",
        "org.thoughtcrime.securesms" to "Signal",
        "com.tencent.mm" to "WeChat",
        // AI chatbots / companions
        "com.openai.chatgpt" to "ChatGPT",
        "com.anthropic.claude" to "Claude",
        "com.google.android.apps.bard" to "Gemini",
        "com.microsoft.copilot" to "Microsoft Copilot",
        "ai.perplexity.app.android" to "Perplexity",
        "com.facebook.meta.ai" to "Meta AI",             // tentative
        "com.xai.grok" to "Grok",                        // tentative
        "ai.character.app" to "Character.AI",
        "ai.replika.app" to "Replika",
        "com.inflection.pi" to "Pi",
        "ai.nomi" to "Nomi",                             // tentative
        "ai.chai" to "Chai",                             // tentative
        "ai.anima" to "Anima",                           // tentative
        "com.deepseek.chat" to "DeepSeek",
    )

    private val PRODUCTION_TARGETS: Set<String> = PRODUCTION_NAMES.keys

    // Debug-only entries so the overlay can be triggered on emulators (Appetize) that don't
    // have the production target apps installed. Stripped from release builds.
    private val DEBUG_EXTRA_TARGETS = mapOf(
        "com.android.chrome" to "Chrome (debug)",
    )

    val TARGET_PACKAGES: Set<String> =
        if (BuildConfig.DEBUG) PRODUCTION_TARGETS + DEBUG_EXTRA_TARGETS.keys else PRODUCTION_TARGETS

    val APP_NAMES: Map<String, String> =
        if (BuildConfig.DEBUG) PRODUCTION_NAMES + DEBUG_EXTRA_TARGETS else PRODUCTION_NAMES

    // Prolific IDs are 24 hex characters (lowercase). Must match the backend check.
    private val PROLIFIC_ID_REGEX = Regex("^[0-9a-f]{24}$")

    fun isValidProlificId(id: String): Boolean = PROLIFIC_ID_REGEX.matches(id)

    fun isTargetApp(packageName: String) = packageName in TARGET_PACKAGES

    fun isSelectedApp(packageName: String, selectedApps: String): Boolean {
        if (selectedApps.isBlank()) return isTargetApp(packageName)
        val selected = com.google.gson.Gson().fromJson(selectedApps, Array<String>::class.java)
        return packageName in selected
    }

    fun getAppName(packageName: String) = APP_NAMES[packageName] ?: packageName

    fun getSelectedAppNames(selectedApps: String): List<String> {
        if (selectedApps.isBlank()) return emptyList()
        val pkgs = com.google.gson.Gson().fromJson(selectedApps, Array<String>::class.java)
        // De-duplicate display names (some apps have multiple packages, e.g. TikTok/Threads).
        return pkgs.mapNotNull { APP_NAMES[it] }.distinct()
    }

    /**
     * Walks the catalog and returns the package names of every target app that's actually
     * installed on this device. Used to auto-populate the tracked-apps list during setup —
     * participants do not deselect; whatever they have installed is what we track.
     */
    fun installedTargetPackages(context: android.content.Context): List<String> {
        val pm = context.packageManager
        return TARGET_PACKAGES.filter { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    /** Display names of installed targets, de-duplicated. */
    fun installedTargetNames(context: android.content.Context): List<String> {
        return installedTargetPackages(context).mapNotNull { APP_NAMES[it] }.distinct()
    }

    /** Sampling window check using minute-of-day. Handles overnight wrap (e.g. 22:00–06:00). */
    fun isWithinSamplingWindow(startMin: Int, endMin: Int): Boolean {
        val cal = Calendar.getInstance()
        val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        return if (startMin <= endMin) {
            nowMin in startMin until endMin
        } else {
            nowMin >= startMin || nowMin < endMin
        }
    }

    /** Length of sampling window in minutes, accounting for overnight wrap. */
    fun windowLengthMinutes(startMin: Int, endMin: Int): Int {
        val raw = endMin - startMin
        return if (raw > 0) raw else raw + 24 * 60
    }

    fun isValidWindow(startMin: Int, endMin: Int): Boolean =
        windowLengthMinutes(startMin, endMin) >= 8 * 60

    fun formatTime12h(minutesOfDay: Int): String {
        val h24 = (minutesOfDay / 60) % 24
        val m = minutesOfDay % 60
        val h12 = ((h24 + 11) % 12) + 1
        val ampm = if (h24 < 12) "AM" else "PM"
        return "%d:%02d %s".format(h12, m, ampm)
    }

    suspend fun canShowPrompt(
        repository: StudyRepository,
        studyDay: Int,
        windowStartMin: Int,
        windowEndMin: Int
    ): Boolean {
        if (!isWithinSamplingWindow(windowStartMin, windowEndMin)) return false

        val todayCount = repository.getPromptedSessionCountForDay(studyDay)
        if (todayCount >= MAX_PROMPTS_PER_DAY) return false

        val lastPromptTime = repository.getLastPromptedSessionOpenTime() ?: return true
        val minutesSinceLast = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastPromptTime)
        return minutesSinceLast >= MIN_INTERVAL_MINUTES
    }

    /**
     * Compute study day from the participant's *studyStartDate* (the moment Day 1 begins).
     * Returns 0 when we're still before studyStartDate (setup/install day). Day 1 begins
     * at studyStartDate, Day 7 ends 7×24h later, Day 8 = endline, etc.
     *
     * Falls back to enrollmentDate when studyStartDate is 0 — that path keeps the old
     * behavior for participants whose row predates the studyStartDate field.
     */
    fun getCurrentStudyDay(enrollmentDate: Long, studyStartDate: Long = 0L): Int {
        val anchor = if (studyStartDate > 0L) studyStartDate else enrollmentDate
        val now = System.currentTimeMillis()
        if (now < anchor) return 0  // pre-start (install/setup day)
        val daysSinceStart = TimeUnit.MILLISECONDS.toDays(now - anchor).toInt()
        // Day 1 is the first 24h after anchor, hence +1.
        return daysSinceStart + 1
    }

    /** Convenience overload that pulls both fields off the Participant. */
    fun getCurrentStudyDay(p: com.smu.studyapp.data.entities.Participant): Int =
        getCurrentStudyDay(p.enrollmentDate, p.studyStartDate)

    /**
     * Compute Day 1's wall-clock start = tomorrow (relative to `nowMs`) at the participant's
     * window-start minute-of-day. Always strictly in the future from `nowMs`.
     */
    fun computeStudyStartDate(nowMs: Long, windowStartMin: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMs
            // Advance to tomorrow, then set to window start.
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, windowStartMin / 60)
            set(Calendar.MINUTE, windowStartMin % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
