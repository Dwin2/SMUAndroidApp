package com.smu.studyapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participant")
data class Participant(
    @PrimaryKey val id: Int = 1,
    val participantCode: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val studyGroup: String = "T", // "T" = Treatment, "C" = Control
    val enrollmentDate: Long = System.currentTimeMillis(),
    val samplingWindowStart: Int = 8,   // hour of day (24h), default 8am
    val samplingWindowEnd: Int = 23,    // hour of day (24h), default 11pm
    val setupComplete: Boolean = false,
    val baselineSurveyComplete: Boolean = false,
    val currentStudyDay: Int = 0,       // 0=baseline, 1-7=study, 8=endline
    val selectedApps: String = ""       // JSON array of selected package names, empty = all
)
