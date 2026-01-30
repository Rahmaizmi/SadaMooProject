package com.example.sadamoo.users.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DetectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detection: Detection)

    @Query("SELECT * FROM detection ORDER BY id DESC")
    fun getAllDetections(): LiveData<List<Detection>>

    @Query("SELECT * FROM detection WHERE userId = :userId ORDER BY id DESC")
    fun getDetectionsByUserId(userId: String): LiveData<List<Detection>>

    @Delete
    suspend fun delete(detection: Detection)

    @Query("DELETE FROM detection")
    suspend fun deleteAll()
}