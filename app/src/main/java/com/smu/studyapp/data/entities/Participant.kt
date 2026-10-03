package com.smu.studyapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participant")
data class Participant(
    @PrimaryKey val id: Int = 1,
    val participantCode: String = "",          // 24-char Prolific ID
    val studyGroup: String = "",               // "T" / "C", assigned by the server's allocation list; "" = not enrolled
    val enrollmentDate: Long = System.currentTimeMillis(),
    val samplingWindowStartMin: Int = 8 * 60,  // minute-of-day, default 08:00
    val samplingWindowEndMin: Int = 23 * 60,   // minute-of-day, default 23:00
    val setupComplete: Boolean = false,
    val currentStudyDay: Int = 0,              // 0=baseline, 1-7=study, 8=endline
    val selectedApps: String = "",             // JSON array of selected package names
    // Wall-clock millis at which the study actually starts (Day 1). Set when setup
    // completes to tomorrow's window-start, so the install day is setup-only.
    val studyStartDate: Long = 0L
)
