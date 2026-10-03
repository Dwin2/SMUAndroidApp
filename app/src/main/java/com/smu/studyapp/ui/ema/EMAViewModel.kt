package com.smu.studyapp.ui.ema

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.smu.studyapp.MyApplication
import com.smu.studyapp.data.entities.SurveyResponse
import com.smu.studyapp.network.SyncWorker
import com.smu.studyapp.utils.SamplingManager
import kotlinx.coroutines.launch

class EMAViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as MyApplication).repository
    private val gson = Gson()
    private val appContext = app.applicationContext
    private var surveyType: String = "EMA_5PM"

    fun init(type: String) { surveyType = type }

    fun submit(responses: Map<String, String>, onDone: () -> Unit) {
        viewModelScope.launch {
            val participant = repo.getParticipant() ?: return@launch
            val day = SamplingManager.getCurrentStudyDay(participant)
            repo.saveSurveyResponse(
                SurveyResponse(
                    participantCode = participant.participantCode,
                    surveyType = surveyType,
                    studyDay = day,
                    responseJson = gson.toJson(responses)
                )
            )
            SyncWorker.triggerNow(appContext)
            onDone()
        }
    }
}
