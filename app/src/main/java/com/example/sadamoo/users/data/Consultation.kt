package com.example.sadamoo.users.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "consultation")
@Parcelize
data class Consultation(
    @PrimaryKey
    val id: String = "",
    val doctorId: String,
    val doctorName: String,
    val lastMessage: String,
    val lastSenderId: String,
    val timestamp: Date,
    val userId: String,
    val userName: String
) : Parcelable