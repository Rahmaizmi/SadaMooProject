package com.example.sadamoo.users.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ConsultationDao {
    @Query("SELECT * FROM consultation ORDER BY timestamp DESC")
    fun getAllConsultations(): LiveData<List<Consultation>>

    @Query("SELECT * FROM consultation WHERE userId = :userId ORDER BY timestamp DESC")
    fun getConsultationsByUserId(userId: String): LiveData<List<Consultation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(consultation: Consultation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(consultations: List<Consultation>)

    @Delete
    suspend fun delete(consultation: Consultation)

    @Query("DELETE FROM consultation")
    suspend fun deleteAll()
}