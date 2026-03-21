package com.smu.studyapp.data.dao

import androidx.room.*
import com.smu.studyapp.data.entities.AppSession
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AppSession)

    @Update
    suspend fun updateSession(session: AppSession)

    @Query("SELECT * FROM app_sessions WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: String): AppSession?

    @Query("SELECT * FROM app_sessions WHERE studyDay = :day ORDER BY openTime DESC")
    suspend fun getSessionsForDay(day: Int): List<AppSession>

    @Query("SELECT * FROM app_sessions ORDER BY openTime DESC")
    fun getAllSessionsFlow(): Flow<List<AppSession>>

    @Query("SELECT COUNT(*) FROM app_sessions WHERE promptShown = 1 AND studyDay = :day")
    suspend fun getPromptedSessionCountForDay(day: Int): Int

    @Query("SELECT MAX(openTime) FROM app_sessions WHERE promptShown = 1")
    suspend fun getLastPromptedSessionOpenTime(): Long?
}
