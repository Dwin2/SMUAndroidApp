package com.smu.studyapp.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smu.studyapp.MyApplication
import com.smu.studyapp.data.entities.Participant
import com.smu.studyapp.network.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SetupViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as MyApplication).repository

    private val _participant = MutableStateFlow<Participant?>(null)
    val participant: StateFlow<Participant?> = _participant

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

    sealed class EnrollState {
        object Idle : EnrollState()
        object Loading : EnrollState()
        object Done : EnrollState()
        data class Error(val message: String) : EnrollState()
    }

    private val _enrollState = MutableStateFlow<EnrollState>(EnrollState.Idle)
    val enrollState: StateFlow<EnrollState> = _enrollState

    /**
     * Enrolls the Prolific ID with the backend, which assigns the condition from the allocation
     * list. The participant row is only saved once a condition comes back, so setup can't
     * continue unassigned. The condition is never shown on screen.
     */
    fun enroll(prolificId: String) {
        if (_enrollState.value == EnrollState.Loading) return
        _enrollState.value = EnrollState.Loading
        viewModelScope.launch {
            when (val r = SyncManager(repo).enroll(prolificId)) {
                is SyncManager.EnrollResult.Assigned -> {
                    val existing = repo.getParticipant()
                    repo.saveParticipant(
                        (existing ?: Participant()).copy(
                            participantCode = prolificId,
                            studyGroup = r.studyGroup
                        )
                    )
                    _enrollState.value = EnrollState.Done
                }
                is SyncManager.EnrollResult.Failed ->
                    _enrollState.value = EnrollState.Error(r.message)
            }
        }
    }

    fun resetEnrollState() {
        _enrollState.value = EnrollState.Idle
    }

    fun saveSamplingWindow(startMin: Int, endMin: Int, selectedApps: List<String>) {
        viewModelScope.launch {
            val existing = repo.getParticipant() ?: return@launch
            val json = com.google.gson.Gson().toJson(selectedApps)
            repo.updateParticipant(
                existing.copy(
                    samplingWindowStartMin = startMin,
                    samplingWindowEndMin = endMin,
                    selectedApps = json
                )
            )
        }
    }

    fun completeSetup() {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val now = System.currentTimeMillis()
            val studyStart = com.smu.studyapp.utils.SamplingManager.computeStudyStartDate(
                now, participant.samplingWindowStartMin
            )
            repo.updateParticipant(
                participant.copy(
                    setupComplete = true,
                    enrollmentDate = now,
                    studyStartDate = studyStart
                )
            )
        }
    }
}
