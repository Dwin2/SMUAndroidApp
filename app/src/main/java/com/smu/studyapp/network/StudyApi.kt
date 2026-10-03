package com.smu.studyapp.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface StudyApi {
    @POST("v1/participants")
    suspend fun registerParticipant(
        @Header("x-api-key") apiKey: String,
        @Body body: ParticipantDto
    ): RegisterResponse

    @POST("v1/sync")
    suspend fun sync(
        @Header("x-api-key") apiKey: String,
        @Body body: SyncRequestDto
    ): SyncResponse

    @GET("v1/participants/{prolificId}/export")
    suspend fun export(
        @Header("x-api-key") apiKey: String,
        @Path("prolificId") prolificId: String
    ): ExportResponse
}
