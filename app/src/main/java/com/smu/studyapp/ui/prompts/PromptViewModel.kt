package com.smu.studyapp.ui.prompts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.smu.studyapp.MyApplication
import com.smu.studyapp.data.entities.SurveyResponse
import com.smu.studyapp.network.SyncWorker
import com.smu.studyapp.utils.NudgeManager
import com.smu.studyapp.utils.SamplingManager
import kotlinx.coroutines.launch

class PromptViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as MyApplication).repository
    private val gson = Gson()
    private val appContext = app.applicationContext

    private var sessionId: String = ""
    private var appPackage: String = ""
    private var promptType: String = ""
    private var mode: String = NudgeManager.MODE_STANDARD
    private var nudgeTrigger: String = NudgeManager.TRIGGER_NONE

    fun init(
        sessionId: String,
        appPackage: String,
        promptType: String,
        mode: String = NudgeManager.MODE_STANDARD,
        nudgeTrigger: String = NudgeManager.TRIGGER_NONE
    ) {
        this.sessionId = sessionId
        this.appPackage = appPackage
        this.promptType = promptType
        this.mode = mode
        this.nudgeTrigger = nudgeTrigger
    }

    fun submitMRP(motivation: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val day = SamplingManager.getCurrentStudyDay(participant)
            repo.saveSurveyResponse(
                SurveyResponse(
                    participantCode = participant.participantCode,
                    surveyType = "MRP",
                    appPackage = appPackage,
                    sessionId = sessionId,
                    studyDay = day,
                    responseJson = gson.toJson(
                        mapOf(
                            "motivation" to motivation,
                            "mode" to mode,
                            "nudge_trigger" to nudgeTrigger
                        )
                    )
                )
            )
            // Any open-prompt submit resets the consecutive-skip counter.
            NudgeManager.onSubmit(appContext)
            SyncWorker.triggerNow(appContext)
            onDone()
        }
    }

    fun submitNP(answer: String, question: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val day = SamplingManager.getCurrentStudyDay(participant)
            repo.saveSurveyResponse(
                SurveyResponse(
                    participantCode = participant.participantCode,
                    surveyType = "NP",
                    appPackage = appPackage,
                    sessionId = sessionId,
                    studyDay = day,
                    responseJson = gson.toJson(
                        mapOf(
                            "question" to question,
                            "answer" to answer,
                            "mode" to mode,
                            "nudge_trigger" to nudgeTrigger
                        )
                    )
                )
            )
            NudgeManager.onSubmit(appContext)
            SyncWorker.triggerNow(appContext)
            onDone()
        }
    }

    fun submitSatisfaction(rating: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val day = SamplingManager.getCurrentStudyDay(participant)
            repo.saveSurveyResponse(
                SurveyResponse(
                    participantCode = participant.participantCode,
                    surveyType = "SATISFACTION",
                    appPackage = appPackage,
                    sessionId = sessionId,
                    studyDay = day,
                    responseJson = gson.toJson(mapOf("satisfaction" to rating))
                )
            )
            // Mark session satisfaction answered
            repo.getSession(sessionId)?.let {
                repo.updateSession(it.copy(satisfactionAnswered = true, synced = false))
            }
            SyncWorker.triggerNow(appContext)
            onDone()
        }
    }
}

/**
 * Saves a "skipped" survey response from the AppMonitorService when the user dismisses
 * an opening prompt (T/C) without answering. Logged so it shows up on the dashboard
 * and counts toward the daily skip cap.
 */
object PromptSkipLogger {
    private val gson = com.google.gson.Gson()

    suspend fun logSkip(
        repo: com.smu.studyapp.data.repository.StudyRepository,
        sessionId: String,
        appPackage: String,
        promptType: String,  // "T" → MRP, "C" → NP
        mode: String = com.smu.studyapp.utils.NudgeManager.MODE_STANDARD,
        nudgeTrigger: String = com.smu.studyapp.utils.NudgeManager.TRIGGER_NONE
    ) {
        val participant = repo.getParticipant() ?: return
        val day = SamplingManager.getCurrentStudyDay(participant)
        val surveyType = if (promptType == "T") "MRP" else "NP"
        repo.saveSurveyResponse(
            SurveyResponse(
                participantCode = participant.participantCode,
                surveyType = surveyType,
                appPackage = appPackage,
                sessionId = sessionId,
                studyDay = day,
                responseJson = gson.toJson(
                    mapOf(
                        "skipped" to true,
                        "mode" to mode,
                        "nudge_trigger" to nudgeTrigger
                    )
                )
            )
        )
    }
}
