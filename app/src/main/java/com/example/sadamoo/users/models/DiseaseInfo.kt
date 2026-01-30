package com.example.sadamoo.users.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiseaseInfo(
    val name: String,
    val scientificName: String,
    val description: String,
    val symptoms: List<String>,
    val causes: List<String>,
    val treatments: List<String>,
    val prevention: List<String>,
    val estimatedLoss: Int,
    val severity: String, // mild, moderate, severe
    val contagious: Boolean
) : Parcelable
