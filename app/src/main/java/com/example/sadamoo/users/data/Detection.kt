package com.example.sadamoo.users.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "detection")
@Parcelize
data class Detection(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val imagePath: String,
    val cattleType: String,
    val description: String,
    val confidence: Float,
    val isHealthy: Boolean,
    val detectedDisease: String?,
    val timestamp: Date
) : Parcelable
