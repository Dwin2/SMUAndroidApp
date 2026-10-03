package com.smu.studyapp.network

import android.util.Log
import com.smu.studyapp.data.entities.AppSession
import com.smu.studyapp.data.entities.Participant
import com.smu.studyapp.data.entities.SurveyResponse
import com.smu.studyapp.data.repository.StudyRepository

class SyncManager(private val repo: StudyRepository) {

    sealed class Result {
        data class Ok(val surveys: Int, val sessions: Int) : Result()
        data class Skipped(val reason: String) : Result()
        data class Failed(val message: String) : Result()
    }

    sealed class EnrollResult {
        data class Assigned(val studyGroup: String) : EnrollResult()
        data class Failed(val message: String) : EnrollResult()
    }

    /**
     * First registration: the server claims an allocation slot for this Prolific ID and returns
     * its condition (or the existing one, if this ID already enrolled — e.g. after a reinstall).
     */
    suspend fun enroll(prolificId: String): EnrollResult {
        if (!NetworkModule.isConfigured) return EnrollResult.Failed("Backend not configured")
        return try {
            val res = NetworkModule.api.registerParticipant(
                NetworkModule.apiKey,
                Participant(participantCode = prolificId).toDto()
            )
            val group = res.studyGroup
            if (res.ok && (group == "T" || group == "C")) EnrollResult.Assigned(group)
            else EnrollResult.Failed("Unexpected server response")
        } catch (t: Throwable) {
            Log.e(TAG, "enroll failed", t)
            EnrollResult.Failed(t.message ?: t.javaClass.simpleName)
        }
    }

    suspend fun registerCurrentParticipant(): Result {
        if (!NetworkModule.isConfigured) return Result.Skipped("Backend URL/key not configured")
        val p = repo.getParticipant() ?: return Result.Skipped("No participant yet")
        if (p.participantCode.isBlank()) return Result.Skipped("Empty Prolific ID")
        return try {
            NetworkModule.api.registerParticipant(NetworkModule.apiKey, p.toDto())
            Log.i(TAG, "Registered participant ${p.participantCode}")
            Result.Ok(0, 0)
        } catch (t: Throwable) {
            Log.e(TAG, "registerParticipant failed", t)
            Result.Failed(t.message ?: t.javaClass.simpleName)
        }
    }

    suspend fun syncNow(): Result {
        if (!NetworkModule.isConfigured) return Result.Skipped("Backend URL/key not configured")
        val participant = repo.getParticipant() ?: return Result.Skipped("No participant yet")
        if (participant.participantCode.isBlank()) return Result.Skipped("Empty Prolific ID")

        // Make sure META exists on the server. Idempotent. The server's condition is the source
        // of truth, so adopt it if the local copy ever differs.
        runCatching {
            NetworkModule.api.registerParticipant(NetworkModule.apiKey, participant.toDto())
        }.onSuccess { res ->
            val group = res.studyGroup
            if ((group == "T" || group == "C") && group != participant.studyGroup) {
                Log.w(TAG, "local studyGroup differed from server; adopting server value")
                repo.updateParticipant(participant.copy(studyGroup = group))
            }
        }.onFailure { Log.w(TAG, "register on sync failed: ${it.message}") }

        val pendingSurveys = repo.getUnsyncedSurveys()
        val pendingSessions = repo.getUnsyncedSessions()
        if (pendingSurveys.isEmpty() && pendingSessions.isEmpty()) {
            return Result.Ok(0, 0)
        }

        return try {
            val response = NetworkModule.api.sync(
                NetworkModule.apiKey,
                SyncRequestDto(
                    prolificId = participant.participantCode,
                    surveys = pendingSurveys.map { it.toDto() },
                    sessions = pendingSessions.map { it.toDto() }
                )
            )
            if (response.ok) {
                repo.markSurveysSynced(pendingSurveys.map { it.id })
                repo.markSessionsSynced(pendingSessions.map { it.sessionId })
                Log.i(TAG, "Synced ${pendingSurveys.size} surveys + ${pendingSessions.size} sessions")
                Result.Ok(pendingSurveys.size, pendingSessions.size)
            } else {
                Result.Failed("Server returned ok=false")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "sync failed", t)
            Result.Failed(t.message ?: t.javaClass.simpleName)
        }
    }

    private companion object { const val TAG = "SyncManager" }
}

private fun Participant.toDto() = ParticipantDto(
    prolificId = participantCode,
    enrollmentDate = enrollmentDate,
    samplingWindowStartMin = samplingWindowStartMin,
    samplingWindowEndMin = samplingWindowEndMin,
    selectedApps = parseSelectedApps(selectedApps),
    currentStudyDay = currentStudyDay
)

private fun parseSelectedApps(json: String): List<String> {
    if (json.isBlank()) return emptyList()
    return try {
        com.google.gson.Gson()
            .fromJson(json, Array<String>::class.java)
            .toList()
    } catch (_: Exception) {
        emptyList()
    }
}

private fun SurveyResponse.toDto() = SurveyResponseDto(
    localId = id,
    participantCode = participantCode,
    surveyType = surveyType,
    appPackage = appPackage,
    sessionId = sessionId,
    studyDay = studyDay,
    timestamp = timestamp,
    responseJson = responseJson
)

private fun AppSession.toDto() = AppSessionDto(
    sessionId = sessionId,
    participantCode = participantCode,
    appPackage = appPackage,
    appName = appName,
    openTime = openTime,
    closeTime = closeTime,
    studyDay = studyDay,
    promptShown = promptShown,
    promptType = promptType,
    satisfactionAnswered = satisfactionAnswered,
    withinSamplingWindow = withinSamplingWindow
)
