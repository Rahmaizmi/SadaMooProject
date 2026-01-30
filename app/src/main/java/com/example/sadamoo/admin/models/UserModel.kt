package com.example.sadamoo.admin.models

import java.io.Serializable
import java.util.Date

data class UserModel(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "user", // "user", "admin", or "doctor"
    var subscriptionStatus: String = "trial", // "trial", "active", "expired"
    var subscriptionType: String? = null, // "monthly", "yearly", null
    val createdAt: Date? = null,
    val lastActive: Date? = null,
    val trialStartDate: Date? = null,
    var subscriptionEndDate: Date? = null,
    var isBanned: Boolean = false,
    var totalScans: Int = 0,
    val photoBase64: String? = null
) : Serializable {

    fun getRoleBadgeText(): String {
        return when (role) {
            "admin" -> "👨‍💼 Admin"
            "doctor" -> "👨‍⚕️ Dokter"
            else -> "👤 User"
        }
    }

    fun getRoleBadgeColor(): String {
        return when (role) {
            "admin" -> "#8B5CF6" // Purple
            "doctor" -> "#10B981" // Green
            else -> "#4A90E2" // Blue
        }
    }

    fun getSubscriptionBadgeText(): String {
        return when (subscriptionStatus) {
            "active" -> "Premium"
            "expired" -> "Berakhir"
            else -> "Trial"
        }
    }
}