package com.smu.studyapp.network

import com.smu.studyapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    val isConfigured: Boolean
        get() = BuildConfig.API_BASE_URL.isNotBlank() && BuildConfig.STUDY_API_KEY.isNotBlank()

    val api: StudyApi by lazy {
        require(isConfigured) {
            "Backend not configured. Set STUDY_API_BASE_URL and STUDY_API_KEY in local.properties."
        }
        val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
            })
            .build()
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudyApi::class.java)
    }

    val apiKey: String get() = BuildConfig.STUDY_API_KEY
}
