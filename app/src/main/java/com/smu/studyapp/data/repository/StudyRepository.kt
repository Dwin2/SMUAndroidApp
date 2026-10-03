package com.smu.studyapp.data.repository

import com.smu.studyapp.data.AppDatabase
import com.smu.studyapp.data.entities.AppSession
import com.smu.studyapp.data.entities.Participant
import com.smu.studyapp.data.entities.SurveyResponse
import kotlinx.coroutines.flow.Flow

class StudyRepository(db: AppDatabase) {
    private val participantDao = db.participantDao()
    private val surveyResponseDao = db.surveyResponseDao()
    private val appSessionDao = db.appSessionDao()

    // Participant
    fun getParticipantFlow(): Flow<Participant?> = participantDao.getParticipantFlow()
    suspend fun getParticipant(): Participant? = participantDao.getParticipant()
    suspend fun saveParticipant(participant: Participant) = participantDao.insertParticipant(participant)
    suspend fun updateParticipant(participant: Participant) = participantDao.updateParticipant(participant)

    // Survey responses
    suspend fun saveSurveyResponse(response: SurveyResponse) = surveyResponseDao.insertResponse(response)
    fun getAllResponses(): Flow<List<SurveyResponse>> = surveyResponseDao.getAllResponses()
    suspend fun getPromptCountForDay(day: Int) = surveyResponseDao.getPromptCountForDay(day)
    suspend fun getLastPromptTimestamp() = surveyResponseDao.getLastPromptTimestamp()
    suspend fun surveysSinceTimestamp(surveyType: String, sinceMs: Long) =
        surveyResponseDao.countSurveysSince(surveyType, sinceMs)
    suspend fun getOpeningResponseForSession(sessionId: String) =
        surveyResponseDao.getOpeningResponseForSession(sessionId)
    suspend fun getSkipCountForDay(day: Int) = surveyResponseDao.getSkipCountForDay(day)

    // App sessions
    suspend fun saveSession(session: AppSession) = appSessionDao.insertSession(session)
    suspend fun updateSession(session: AppSession) = appSessionDao.updateSession(session)
    suspend fun getSession(sessionId: String) = appSessionDao.getSession(sessionId)
    fun getAllSessionsFlow(): Flow<List<AppSession>> = appSessionDao.getAllSessionsFlow()
    suspend fun getPromptedSessionCountForDay(day: Int) = appSessionDao.getPromptedSessionCountForDay(day)
    suspend fun getLastPromptedSessionOpenTime() = appSessionDao.getLastPromptedSessionOpenTime()
    suspend fun getSessionsForDay(day: Int) = appSessionDao.getSessionsForDay(day)

    // Sync
    suspend fun getUnsyncedSurveys(limit: Int = 500) = surveyResponseDao.getUnsynced(limit)
    suspend fun getUnsyncedSessions(limit: Int = 500) = appSessionDao.getUnsynced(limit)
    suspend fun markSurveysSynced(ids: List<Long>) {
        if (ids.isNotEmpty()) surveyResponseDao.markSynced(ids)
    }
    suspend fun markSessionsSynced(sessionIds: List<String>) {
        if (sessionIds.isNotEmpty()) appSessionDao.markSynced(sessionIds)
    }
    suspend fun unsyncedCount(): Int =
        surveyResponseDao.unsyncedCount() + appSessionDao.unsyncedCount()
}
