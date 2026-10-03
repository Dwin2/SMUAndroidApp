package com.smu.studyapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survey_responses")
data class SurveyResponse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val participantCode: String,
    val surveyType: String,      // "BASELINE","MRP","NP","SATISFACTION","EMA_5PM","EMA_9PM","ENDLINE","FOLLOWUP"
    val appPackage: String = "", // which social media app triggered it
    val sessionId: String = "",  // links open prompt to close satisfaction
    val studyDay: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val responseJson: String,    // JSON blob of question -> answer pairs
    val synced: Boolean = false  // true once successfully uploaded to AWS
)
