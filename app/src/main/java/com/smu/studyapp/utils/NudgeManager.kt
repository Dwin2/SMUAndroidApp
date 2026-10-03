package com.smu.studyapp.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tracks "nudge mode" state for the opening prompts (MRP / NP). Nudge mode re-anchors a
 * participant whose behavior suggests disengagement by adding a warm note above the question
 * and relabeling Skip → "Skip for now" (which lets the app open normally instead of closing it).
 *
 * Two triggers cause the NEXT opening prompt to render in nudge mode:
 *  - daily_first   : the first Standard-mode skip of the day arms a nudge for the next open of
 *                    that SAME app.
 *  - consecutive_3 : [CONSECUTIVE_SKIP_THRESHOLD] Standard-mode skips in a row across ANY apps
 *                    arm a nudge for the next open of any designated app. The counter resets on
 *                    any open-prompt Submit.
 *
 * After a nudge prompt is shown (whether the participant submits or skips-for-now) the counters
 * reset and normal Standard-mode behavior resumes.
 *
 * State is persisted to SharedPreferences so it survives the social-media app close/reopen
 * cycle (and process restarts) — the daily_first trigger depends on that survival.
 *
 * The decision logic is split into pure functions ([decide], [afterStandardSkip], [afterSubmit],
 * [afterNudgeShown]) operating on an immutable [State], so it can be unit-tested without Android.
 */
object NudgeManager {
    // Spec prose says "2 in a row" but the logging enum is `consecutive_3` and the worked
    // example (skip IG → FB → TT → next open nudges) implies 3. Flip this if 2 was intended.
    const val CONSECUTIVE_SKIP_THRESHOLD = 3

    const val MODE_STANDARD = "standard"
    const val MODE_NUDGE = "nudge"
    const val TRIGGER_NONE = "none"
    const val TRIGGER_DAILY_FIRST = "daily_first"
    const val TRIGGER_CONSECUTIVE = "consecutive_3"

    private const val PREFS = "nudge_state"
    private const val KEY_CONSECUTIVE = "consecutive_skips"
    private const val KEY_ARMED_APP = "daily_first_armed_app"
    private const val KEY_FIRST_SKIP_DATE = "daily_first_skip_date"

    /** Immutable snapshot of nudge state. */
    data class State(
        val consecutiveSkips: Int = 0,
        val armedApp: String? = null,
        val firstSkipDate: String = ""
    )

    // ─────────────────────────── Pure logic (unit-tested) ───────────────────────────

    /** Decide whether a prompt about to be shown for [packageName] is standard or nudge. */
    fun decide(state: State, packageName: String): Pair<String, String> = when {
        // daily_first is highest priority per spec.
        state.armedApp == packageName -> MODE_NUDGE to TRIGGER_DAILY_FIRST
        state.consecutiveSkips >= CONSECUTIVE_SKIP_THRESHOLD -> MODE_NUDGE to TRIGGER_CONSECUTIVE
        else -> MODE_STANDARD to TRIGGER_NONE
    }

    /** A Standard-mode skip: bump the consecutive counter; arm daily_first if it's the first today. */
    fun afterStandardSkip(state: State, packageName: String, today: String): State {
        val isFirstToday = state.firstSkipDate != today
        return state.copy(
            consecutiveSkips = state.consecutiveSkips + 1,
            armedApp = if (isFirstToday) packageName else state.armedApp,
            firstSkipDate = if (isFirstToday) today else state.firstSkipDate
        )
    }

    /** An open-prompt Submit resets the consecutive-skip counter. */
    fun afterSubmit(state: State): State = state.copy(consecutiveSkips = 0)

    /** A nudge prompt was shown: reset the consecutive counter and disarm daily_first. */
    fun afterNudgeShown(state: State, trigger: String): State = state.copy(
        consecutiveSkips = 0,
        armedApp = if (trigger == TRIGGER_DAILY_FIRST) null else state.armedApp
    )

    // ─────────────────────── SharedPreferences-backed public API ───────────────────────

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun load(ctx: Context): State {
        val p = prefs(ctx)
        return State(
            consecutiveSkips = p.getInt(KEY_CONSECUTIVE, 0),
            armedApp = p.getString(KEY_ARMED_APP, null),
            firstSkipDate = p.getString(KEY_FIRST_SKIP_DATE, "") ?: ""
        )
    }

    private fun save(ctx: Context, state: State) {
        prefs(ctx).edit()
            .putInt(KEY_CONSECUTIVE, state.consecutiveSkips)
            .putString(KEY_ARMED_APP, state.armedApp)
            .putString(KEY_FIRST_SKIP_DATE, state.firstSkipDate)
            .apply()
    }

    @Synchronized
    fun decideMode(ctx: Context, packageName: String): Pair<String, String> =
        decide(load(ctx), packageName)

    @Synchronized
    fun onStandardSkip(ctx: Context, packageName: String) =
        save(ctx, afterStandardSkip(load(ctx), packageName, today()))

    @Synchronized
    fun onSubmit(ctx: Context) = save(ctx, afterSubmit(load(ctx)))

    @Synchronized
    fun onNudgeShown(ctx: Context, trigger: String) =
        save(ctx, afterNudgeShown(load(ctx), trigger))
}
