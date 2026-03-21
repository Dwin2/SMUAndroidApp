package com.smu.studyapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_sessions")
data class AppSession(
    @PrimaryKey val sessionId: String,
    val participantCode: String,
    val appPackage: String,
    val appName: String,
    val openTime: Long,
    val closeTime: Long = 0L,
    val studyDay: Int,
    val promptShown: Boolean = false,   // whether MRP/NP was shown at open
    val promptType: String = "",        // "MRP" or "NP" or ""
    val satisfactionAnswered: Boolean = false,
    val withinSamplingWindow: Boolean = true
)
