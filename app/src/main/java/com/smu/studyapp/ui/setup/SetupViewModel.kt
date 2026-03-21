package com.smu.studyapp.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smu.studyapp.MyApplication
import com.smu.studyapp.data.entities.Participant
import com.smu.studyapp.data.entities.SurveyResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SetupViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as MyApplication).repository

    private val _participant = MutableStateFlow<Participant?>(null)
    val participant: StateFlow<Participant?> = _participant

    // False until the first DB emission arrives — prevents premature routing
    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded

    init {
        viewModelScope.launch {
            repo.getParticipantFlow().collect {
                _participant.value = it
                _loaded.value = true
            }
        }
    }

    fun saveDemographics(
        name: String,
        age: Int,
        gender: String,
        participantCode: String,
        windowStart: Int,
        windowEnd: Int
    ) {
        viewModelScope.launch {
            val existing = repo.getParticipant()
            val p = (existing ?: Participant()).copy(
                name = name,
                age = age,
                gender = gender,
                participantCode = participantCode,
                samplingWindowStart = windowStart,
                samplingWindowEnd = windowEnd
            )
            repo.saveParticipant(p)
        }
    }

    fun saveSelectedApps(packages: List<String>) {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val json = com.google.gson.Gson().toJson(packages)
            repo.updateParticipant(participant.copy(selectedApps = json))
        }
    }

    fun completeSetup() {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            repo.updateParticipant(
                participant.copy(
                    setupComplete = true,
                    enrollmentDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveBaselineSurvey(responses: Map<String, String>) {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val json = com.google.gson.Gson().toJson(responses)
            repo.saveSurveyResponse(
                SurveyResponse(
                    participantCode = participant.participantCode,
                    surveyType = "BASELINE",
                    studyDay = 0,
                    responseJson = json
                )
            )
            repo.updateParticipant(
                participant.copy(
                    baselineSurveyComplete = true
                )
            )
        }
    }
}
