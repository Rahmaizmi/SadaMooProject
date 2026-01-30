package com.example.sadamoo.admin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class PackageModel(
    var id: String,
    val name: String,
    val description: String,
    val price: Int,
    val duration: Int,
    val durationType: String,
    val features: List<String>,
    val isActive: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
    var subscriberCount: Int
) : Parcelable {

    fun getFormattedPrice(): String {
        return "Rp ${String.format("%,d", price)}"
    }

    fun getDurationText(): String {
        return "$duration ${if (duration > 1) "${durationType}s" else durationType}"
    }

    fun getPricePerMonth(): Int {
        return when (durationType) {
            "year" -> price / (duration * 12)
            else -> price / duration
        }
    }
}
