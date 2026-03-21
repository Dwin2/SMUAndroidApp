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

    @Query("SELECT MAX(timestamp) FROM survey_responses WHERE surveyType IN ('MRP','NP')")
    suspend fun getLastPromptTimestamp(): Long?
}
