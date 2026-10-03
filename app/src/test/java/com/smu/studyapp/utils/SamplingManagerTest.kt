package com.smu.studyapp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the deterministic SamplingManager helpers. */
class SamplingManagerTest {

    @Test
    fun validProlificId_is24Hex() {
        assertTrue(SamplingManager.isValidProlificId("5a9d64f5f6dfdd0001eaa73d"))
        assertFalse(SamplingManager.isValidProlificId("short"))
        assertFalse(SamplingManager.isValidProlificId("5a9d64f5f6dfdd0001eaa73"))   // 23 chars
        assertFalse(SamplingManager.isValidProlificId("5a9d64f5f6dfdd0001eaa73d!"))  // bad char
        assertFalse(SamplingManager.isValidProlificId("5a9d64f5f6dfdd0001eaa7zz"))   // 24 chars, not hex
        assertFalse(SamplingManager.isValidProlificId("5a9d64f5f6dfdd0001eaa73d5"))  // 25 chars
    }

    @Test
    fun windowLength_handlesOvernightWrap() {
        assertEquals(15 * 60, SamplingManager.windowLengthMinutes(8 * 60, 23 * 60))  // 08:00–23:00
        assertEquals(8 * 60, SamplingManager.windowLengthMinutes(22 * 60, 6 * 60))   // 22:00–06:00 wrap
    }

    @Test
    fun isValidWindow_requiresAtLeastEightHours() {
        assertTrue(SamplingManager.isValidWindow(8 * 60, 23 * 60))
        assertTrue(SamplingManager.isValidWindow(22 * 60, 6 * 60))   // 8h overnight
        assertFalse(SamplingManager.isValidWindow(9 * 60, 14 * 60))  // 5h
    }

    @Test
    fun formatTime12h_rendersAmPm() {
        assertEquals("12:00 AM", SamplingManager.formatTime12h(0))
        assertEquals("8:00 AM", SamplingManager.formatTime12h(8 * 60))
        assertEquals("12:00 PM", SamplingManager.formatTime12h(12 * 60))
        assertEquals("11:30 PM", SamplingManager.formatTime12h(23 * 60 + 30))
    }

    @Test
    fun getCurrentStudyDay_beforeStart_isZero() {
        val future = System.currentTimeMillis() + 24L * 60 * 60 * 1000
        assertEquals(0, SamplingManager.getCurrentStudyDay(enrollmentDate = 0L, studyStartDate = future))
    }

    @Test
    fun getCurrentStudyDay_dayOne_onStart() {
        val now = System.currentTimeMillis()
        // started a few seconds ago → Day 1
        assertEquals(1, SamplingManager.getCurrentStudyDay(enrollmentDate = 0L, studyStartDate = now - 5_000))
        // started just over 3 days ago → Day 4
        val threeDays = now - (3L * 24 * 60 * 60 * 1000 + 60_000)
        assertEquals(4, SamplingManager.getCurrentStudyDay(enrollmentDate = 0L, studyStartDate = threeDays))
    }

    @Test
    fun computeStudyStartDate_isInFuture() {
        val now = System.currentTimeMillis()
        val start = SamplingManager.computeStudyStartDate(now, 8 * 60)
        assertTrue("Day 1 start must be strictly after now", start > now)
    }
}
