package com.smu.studyapp.data.dao

import androidx.room.*
import com.smu.studyapp.data.entities.Participant
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participant WHERE id = 1")
    fun getParticipantFlow(): Flow<Participant?>

    @Query("SELECT * FROM participant WHERE id = 1")
    suspend fun getParticipant(): Participant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: Participant)

    @Update
    suspend fun updateParticipant(participant: Participant)
}
