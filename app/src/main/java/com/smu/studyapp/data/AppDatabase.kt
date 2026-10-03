package com.smu.studyapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smu.studyapp.data.dao.AppSessionDao
import com.smu.studyapp.data.dao.ParticipantDao
import com.smu.studyapp.data.dao.SurveyResponseDao
import com.smu.studyapp.data.entities.AppSession
import com.smu.studyapp.data.entities.Participant
import com.smu.studyapp.data.entities.SurveyResponse

@Database(
    entities = [Participant::class, SurveyResponse::class, AppSession::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun participantDao(): ParticipantDao
    abstract fun surveyResponseDao(): SurveyResponseDao
    abstract fun appSessionDao(): AppSessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smu_study_db"
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
