package com.smu.studyapp.utils

import com.smu.studyapp.utils.NudgeManager.MODE_NUDGE
import com.smu.studyapp.utils.NudgeManager.MODE_STANDARD
import com.smu.studyapp.utils.NudgeManager.TRIGGER_CONSECUTIVE
import com.smu.studyapp.utils.NudgeManager.TRIGGER_DAILY_FIRST
import com.smu.studyapp.utils.NudgeManager.TRIGGER_NONE
import com.smu.studyapp.utils.NudgeManager.State
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for the pure nudge state machine. */
class NudgeManagerTest {

    private val IG = "com.instagram.android"
    private val FB = "com.facebook.katana"
    private val TT = "com.zhiliaoapp.musically"
    private val DAY = "2026-06-24"

    @Test
    fun freshState_isStandard() {
        assertEquals(MODE_STANDARD to TRIGGER_NONE, NudgeManager.decide(State(), IG))
    }

    @Test
    fun firstSkipOfDay_armsDailyFirst_forSameApp() {
        val after = NudgeManager.afterStandardSkip(State(), IG, DAY)
        assertEquals(IG, after.armedApp)
        assertEquals(1, after.consecutiveSkips)
        // Re-opening the SAME app → nudge via daily_first.
        assertEquals(MODE_NUDGE to TRIGGER_DAILY_FIRST, NudgeManager.decide(after, IG))
        // A DIFFERENT app does not get the daily_first nudge (count still < threshold).
        assertEquals(MODE_STANDARD to TRIGGER_NONE, NudgeManager.decide(after, FB))
    }

    @Test
    fun dailyFirst_armsOnlyOncePerDay() {
        var s = NudgeManager.afterStandardSkip(State(), IG, DAY)   // arms IG
        s = NudgeManager.afterStandardSkip(s, FB, DAY)             // same day → must NOT re-arm to FB
        assertEquals(IG, s.armedApp)
        assertEquals(2, s.consecutiveSkips)
    }

    @Test
    fun consecutiveThreeSkips_acrossApps_armsConsecutiveNudge() {
        // skip IG → FB → TT (3 in a row), next open of ANY non-armed app nudges.
        var s = NudgeManager.afterStandardSkip(State(), IG, DAY)
        s = NudgeManager.afterStandardSkip(s, FB, DAY)
        s = NudgeManager.afterStandardSkip(s, TT, DAY)
        assertEquals(3, s.consecutiveSkips)
        // A different (non-armed) app fires consecutive_3.
        assertEquals(MODE_NUDGE to TRIGGER_CONSECUTIVE, NudgeManager.decide(s, "com.snapchat.android"))
        // The armed app (IG) still resolves to daily_first first (higher priority).
        assertEquals(MODE_NUDGE to TRIGGER_DAILY_FIRST, NudgeManager.decide(s, IG))
    }

    @Test
    fun belowThreshold_staysStandard() {
        var s = NudgeManager.afterStandardSkip(State(), IG, DAY)
        s = NudgeManager.afterStandardSkip(s, FB, DAY)            // consecutive = 2 (< 3)
        assertEquals(MODE_STANDARD to TRIGGER_NONE, NudgeManager.decide(s, TT))
    }

    @Test
    fun submit_resetsConsecutiveCounter_butKeepsDailyFirstArm() {
        var s = NudgeManager.afterStandardSkip(State(), IG, DAY)  // arms IG, consec 1
        s = NudgeManager.afterStandardSkip(s, FB, DAY)            // consec 2
        s = NudgeManager.afterSubmit(s)                          // submit resets consec
        assertEquals(0, s.consecutiveSkips)
        // daily_first arm survives a submit (it's tied to next open of that app, not the counter).
        assertEquals(IG, s.armedApp)
        assertEquals(MODE_NUDGE to TRIGGER_DAILY_FIRST, NudgeManager.decide(s, IG))
    }

    @Test
    fun nudgeShown_dailyFirst_disarmsAndResets() {
        val armed = NudgeManager.afterStandardSkip(State(), IG, DAY)
        val after = NudgeManager.afterNudgeShown(armed, TRIGGER_DAILY_FIRST)
        assertEquals(0, after.consecutiveSkips)
        assertNull(after.armedApp)
        assertEquals(MODE_STANDARD to TRIGGER_NONE, NudgeManager.decide(after, IG))
    }

    @Test
    fun nudgeShown_consecutive_resetsCounter_keepsArmedApp() {
        var s = NudgeManager.afterStandardSkip(State(), IG, DAY)  // arms IG
        s = NudgeManager.afterStandardSkip(s, FB, DAY)
        s = NudgeManager.afterStandardSkip(s, TT, DAY)            // consec 3
        val after = NudgeManager.afterNudgeShown(s, TRIGGER_CONSECUTIVE)
        assertEquals(0, after.consecutiveSkips)
        // consecutive nudge does not consume the daily_first arm.
        assertEquals(IG, after.armedApp)
    }

    @Test
    fun newDay_reArmsDailyFirst() {
        val day1 = NudgeManager.afterStandardSkip(State(), IG, "2026-06-24")
        val day2 = NudgeManager.afterStandardSkip(day1, FB, "2026-06-25")  // new day → re-arm to FB
        assertEquals(FB, day2.armedApp)
    }
}
