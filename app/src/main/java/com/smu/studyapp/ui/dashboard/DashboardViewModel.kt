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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

private data class DashboardData(
    val participant: Participant?,
    val studyDay: Int,
    val todaySessions: List<AppSession>,
    val todayPromptCount: Int,
    val totalSessions: Int,
    val recentSessions: List<AppSession>,
    val promptHistory: List<SurveyResponse>,
    val trackedAppNames: List<String>
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as MyApplication).repository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun resetStudy(onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(getApplication()).clearAllTables()
            }
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
                    SamplingManager.getCurrentStudyDay(it)
                } ?: 0

                val todaySessions = allSessions.filter { it.studyDay == day }
                // Group responses by session: each session's open+close prompts together
                val promptHistory = allResponses
                    .filter { it.surveyType in listOf("MRP", "NP", "SATISFACTION") }
                    .sortedByDescending { it.timestamp }
                    .take(50)
                // Only OPEN prompts count toward the 15/day cap (close prompts are
                // separate). Skips count too, but capped at MAX_SKIPS_COUNTED_PER_DAY.
                val todayOpenResponses = allResponses.filter {
                    it.studyDay == day && it.surveyType in listOf("MRP", "NP")
                }
                val todaySkipsLogged = todayOpenResponses.count {
                    it.responseJson.contains("\"skipped\":true")
                }
                val countedSkips = minOf(todaySkipsLogged, SamplingManager.MAX_SKIPS_COUNTED_PER_DAY)
                val answeredOpens = todayOpenResponses.size - todaySkipsLogged
                val todayPromptCount = (answeredOpens + countedSkips)
                    .coerceAtMost(SamplingManager.MAX_PROMPTS_PER_DAY)
                val trackedAppNames = SamplingManager.getSelectedAppNames(
                    participant?.selectedApps ?: ""
                )

                DashboardData(
                    participant = participant,
                    studyDay = day,
                    todaySessions = todaySessions,
                    todayPromptCount = todayPromptCount,
                    totalSessions = allSessions.size,
                    recentSessions = allSessions.take(20),
                    promptHistory = promptHistory,
                    trackedAppNames = trackedAppNames
                )
            }.collect { d ->
                _uiState.value = _uiState.value.copy(
                    participant = d.participant,
                    studyDay = d.studyDay,
                    todaySessions = d.todaySessions,
                    todayPromptCount = d.todayPromptCount,
                    totalSessions = d.totalSessions,
                    recentSessions = d.recentSessions,
                    promptHistory = d.promptHistory,
                    trackedAppNames = d.trackedAppNames
                )
            }
        }
    }
}
