package com.smu.studyapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smu.studyapp.MyApplication
import com.smu.studyapp.data.AppDatabase
import com.smu.studyapp.data.entities.AppSession
import com.smu.studyapp.data.entities.Participant
import com.smu.studyapp.data.entities.SurveyResponse
import com.smu.studyapp.utils.SamplingManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val participant: Participant? = null,
    val studyDay: Int = 0,
    val todaySessions: List<AppSession> = emptyList(),
    val todayPromptCount: Int = 0,
    val totalSessions: Int = 0,
    val recentSessions: List<AppSession> = emptyList(),
    val promptHistory: List<SurveyResponse> = emptyList(),
    val trackedAppNames: List<String> = emptyList()
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as MyApplication).repository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun resetStudy(onDone: () -> Unit) {
        viewModelScope.launch {
            AppDatabase.getDatabase(getApplication()).clearAllTables()
            onDone()
        }
    }

    init {
        viewModelScope.launch {
            combine(
                repo.getParticipantFlow(),
                repo.getAllSessionsFlow(),
                repo.getAllResponses()
            ) { participant, allSessions, allResponses ->
                val day = participant?.let {
                    SamplingManager.getCurrentStudyDay(it.enrollmentDate)
                } ?: 0

                val todaySessions = allSessions.filter { it.studyDay == day }
                // Group responses by session: each session's open+close prompts together
                val promptHistory = allResponses
                    .filter { it.surveyType in listOf("MRP", "NP", "SATISFACTION") }
                    .sortedByDescending { it.timestamp }
                    .take(50)
                // Opening prompts sent (regardless of answered) + satisfaction surveys shown
                val openingPrompts = todaySessions.count { it.promptShown }
                val closingPrompts = allResponses.count { it.surveyType == "SATISFACTION" && it.studyDay == day }
                val todayPromptCount = openingPrompts + closingPrompts
                val trackedAppNames = SamplingManager.getSelectedAppNames(
                    participant?.selectedApps ?: ""
                )

                DashboardUiState(
                    participant = participant,
                    studyDay = day,
                    todaySessions = todaySessions,
                    todayPromptCount = todayPromptCount,
                    totalSessions = allSessions.size,
                    recentSessions = allSessions.take(20),
                    promptHistory = promptHistory,
                    trackedAppNames = trackedAppNames
                )
            }.collect { _uiState.value = it }
        }
    }
}
