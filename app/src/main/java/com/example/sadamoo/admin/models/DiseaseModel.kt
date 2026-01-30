package com.example.sadamoo.admin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.*

@Parcelize
data class DiseaseModel(
    var id: String,
    val name: String,
    val scientificName: String,
    val description: String,
    val symptoms: List<String>,
    val causes: List<String>,
    val treatments: List<String>,
    val prevention: List<String>,
    val severity: String, // "mild", "moderate", "severe"
    val isActive: Boolean,
    val estimatedLoss: Int,
    val recoveryTime: String,
    val contagious: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
    var detectionCount: Int
) : Parcelable {

    fun getFormattedLoss(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(estimatedLoss).replace("IDR", "Rp")
    }

    fun getSeverityColor(): String {
        return when (severity) {
            "mild" -> "#4CAF50"
            "moderate" -> "#FF9800"
            "severe" -> "#F44336"
            else -> "#757575"
        }
    }

    fun getSeverityText(): String {
        return when (severity) {
            "mild" -> "Ringan"
            "moderate" -> "Sedang"
            "severe" -> "Berat"
            else -> "Unknown"
        }
    }

    fun getContagiousText(): String {
        return if (contagious) "Menular" else "Tidak Menular"
    }
}
