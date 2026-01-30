package com.example.sadamoo.users.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imagePath: String,
    val result: String,
    val confidence: Float,
    val timestamp: Date,
    val diseaseInfo: String?
)
