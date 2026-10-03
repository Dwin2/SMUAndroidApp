package com.smu.studyapp.network

// No studyGroup: the server assigns it from the allocation list and returns it in
// RegisterResponse. The app never chooses a condition.
data class ParticipantDto(
    val prolificId: String,
    val enrollmentDate: Long,
    val samplingWindowStartMin: Int,
    val samplingWindowEndMin: Int,
    val selectedApps: List<String>,
    val currentStudyDay: Int,
    val appVersion: String? = null
)

data class SurveyResponseDto(
    val localId: Long,
    val participantCode: String,
    val surveyType: String,
    val appPackage: String,
    val sessionId: String,
    val studyDay: Int,
    val timestamp: Long,
    val responseJson: String
)

data class AppSessionDto(
    val sessionId: String,
    val participantCode: String,
    val appPackage: String,
    val appName: String,
    val openTime: Long,
    val closeTime: Long,
    val studyDay: Int,
    val promptShown: Boolean,
    val promptType: String,
    val satisfactionAnswered: Boolean,
    val withinSamplingWindow: Boolean
)

data class SyncRequestDto(
    val prolificId: String,
    val surveys: List<SurveyResponseDto>,
    val sessions: List<AppSessionDto>
)

data class RegisterResponse(val ok: Boolean, val prolificId: String?, val studyGroup: String?)
data class SyncResponse(val ok: Boolean, val written: Int, val surveys: Int, val sessions: Int)
data class ExportResponse(
    val prolificId: String,
    val counts: Counts,
    val meta: Map<String, Any>?,
    val surveys: List<Map<String, Any>>,
    val sessions: List<Map<String, Any>>
) {
    data class Counts(val surveys: Int, val sessions: Int)
}
