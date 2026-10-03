package com.smu.studyapp.data.dao

import androidx.room.*
import com.smu.studyapp.data.entities.SurveyResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyResponseDao {
    @Insert
    suspend fun insertResponse(response: SurveyResponse)

    @Query("SELECT * FROM survey_responses ORDER BY timestamp DESC")
    fun getAllResponses(): Flow<List<SurveyResponse>>

    @Query("SELECT * FROM survey_responses WHERE studyDay = :day ORDER BY timestamp DESC")
    suspend fun getResponsesForDay(day: Int): List<SurveyResponse>

    @Query("SELECT COUNT(*) FROM survey_responses WHERE surveyType IN ('MRP','NP') AND studyDay = :day")
    suspend fun getPromptCountForDay(day: Int): Int

    @Query("SELECT * FROM survey_responses WHERE sessionId = :sessionId AND surveyType IN ('MRP','NP') ORDER BY timestamp DESC LIMIT 1")
    suspend fun getOpeningResponseForSession(sessionId: String): SurveyResponse?

    @Query("SELECT COUNT(*) FROM survey_responses WHERE surveyType IN ('MRP','NP') AND studyDay = :day AND responseJson LIKE '%\"skipped\":true%'")
    suspend fun getSkipCountForDay(day: Int): Int

    @Query("SELECT MAX(timestamp) FROM survey_responses WHERE surveyType IN ('MRP','NP')")
    suspend fun getLastPromptTimestamp(): Long?

    @Query("SELECT COUNT(*) FROM survey_responses WHERE surveyType = :surveyType AND timestamp >= :sinceMs")
    suspend fun countSurveysSince(surveyType: String, sinceMs: Long): Int

    @Query("SELECT * FROM survey_responses WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsynced(limit: Int = 500): List<SurveyResponse>

    @Query("UPDATE survey_responses SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM survey_responses WHERE synced = 0")
    suspend fun unsyncedCount(): Int
}
