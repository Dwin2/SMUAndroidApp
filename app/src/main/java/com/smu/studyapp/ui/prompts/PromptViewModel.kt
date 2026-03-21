package com.smu.studyapp.ui.prompts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.smu.studyapp.MyApplication
import com.smu.studyapp.data.entities.SurveyResponse
import com.smu.studyapp.utils.SamplingManager
import kotlinx.coroutines.launch

class PromptViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as MyApplication).repository
    private val gson = Gson()

    private var sessionId: String = ""
    private var appPackage: String = ""
    private var promptType: String = ""

    fun init(sessionId: String, appPackage: String, promptType: String) {
        this.sessionId = sessionId
        this.appPackage = appPackage
        this.promptType = promptType
    }

    fun submitMRP(motivation: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val day = SamplingManager.getCurrentStudyDay(participant.enrollmentDate)
            repo.saveSurveyResponse(
                SurveyResponse(
                    participantCode = participant.participantCode,
                    surveyType = "MRP",
                    appPackage = appPackage,
                    sessionId = sessionId,
                    studyDay = day,
                    responseJson = gson.toJson(mapOf("motivation" to motivation))
                )
            )
            onDone()
        }
    }

    fun submitNP(answer: String, question: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val day = SamplingManager.getCurrentStudyDay(participant.enrollmentDate)
            repo.saveSurveyResponse(
                SurveyResponse(
                    participantCode = participant.participantCode,
                    surveyType = "NP",
                    appPackage = appPackage,
                    sessionId = sessionId,
                    studyDay = day,
                    responseJson = gson.toJson(mapOf("question" to question, "answer" to answer))
                )
            )
            onDone()
        }
    }

    fun submitSatisfaction(rating: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val day = SamplingManager.getCurrentStudyDay(participant.enrollmentDate)
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
                repo.updateSession(it.copy(satisfactionAnswered = true))
            }
            onDone()
        }
    }
}
