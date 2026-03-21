package com.smu.studyapp.utils

import android.content.Context
import com.smu.studyapp.data.repository.StudyRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit

object SamplingManager {

    const val MAX_PROMPTS_PER_DAY = 15
    const val MIN_INTERVAL_MINUTES = 60L

    val TARGET_PACKAGES = setOf(
        "com.facebook.katana",        // Facebook
        "com.instagram.android",      // Instagram
        "com.google.android.youtube", // YouTube
        "com.zhiliaoapp.musically",   // TikTok
        "com.snapchat.android",       // Snapchat
        "com.twitter.android",        // X/Twitter
        "com.whatsapp",               // WhatsApp
        "com.discord",                // Discord
    )

    val APP_NAMES = mapOf(
        "com.facebook.katana" to "Facebook",
        "com.instagram.android" to "Instagram",
        "com.google.android.youtube" to "YouTube",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.snapchat.android" to "Snapchat",
        "com.twitter.android" to "X/Twitter",
        "com.whatsapp" to "WhatsApp",
        "com.discord" to "Discord",
    )

    fun isTargetApp(packageName: String) = packageName in TARGET_PACKAGES

    fun isSelectedApp(packageName: String, selectedApps: String): Boolean {
        if (selectedApps.isBlank()) return isTargetApp(packageName)
        val selected = com.google.gson.Gson().fromJson(selectedApps, Array<String>::class.java)
        return packageName in selected
    }

    fun getAppName(packageName: String) = APP_NAMES[packageName] ?: packageName

    fun getSelectedAppNames(selectedApps: String): List<String> {
        if (selectedApps.isBlank()) return APP_NAMES.values.toList()
        val pkgs = com.google.gson.Gson().fromJson(selectedApps, Array<String>::class.java)
        return pkgs.mapNotNull { APP_NAMES[it] }
    }

    fun isWithinSamplingWindow(windowStartHour: Int, windowEndHour: Int): Boolean {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return hour >= windowStartHour && hour < windowEndHour
    }

    suspend fun canShowPrompt(
        repository: StudyRepository,
        studyDay: Int,
        windowStartHour: Int,
        windowEndHour: Int
    ): Boolean {
        if (!isWithinSamplingWindow(windowStartHour, windowEndHour)) return false

        val todayCount = repository.getPromptedSessionCountForDay(studyDay)
        if (todayCount >= MAX_PROMPTS_PER_DAY) return false

        val lastPromptTime = repository.getLastPromptedSessionOpenTime() ?: return true
        val minutesSinceLast = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastPromptTime)
        return minutesSinceLast >= MIN_INTERVAL_MINUTES
    }

    fun getCurrentStudyDay(enrollmentDate: Long): Int {
        val daysDiff = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - enrollmentDate).toInt()
        return daysDiff // 0=baseline day, 1-7=study days, 8=endline, 30=followup
    }
}
