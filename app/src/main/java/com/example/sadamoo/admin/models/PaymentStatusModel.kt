package com.example.sadamoo.users.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.*

@Parcelize
data class PaymentStatusModel(
    val id: String,
    val paymentCode: String,
    val packageType: String,
    val packageDuration: Int,
    val totalAmount: Int,
    val status: String, // pending, verified, rejected
    val submittedAt: Date?,
    val verifiedAt: Date?,
    val notes: String,
    val paymentProofUrl: String
) : Parcelable {

    fun getFormattedAmount(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(totalAmount).replace("IDR", "Rp")
    }

    fun getStatusText(): String {
        return when (status) {
            "pending" -> "Menunggu Verifikasi"
            "verified" -> "Terverifikasi"
            "rejected" -> "Ditolak"
            else -> "Status Tidak Diketahui"
        }
    }

    fun getStatusColor(): String {
        return when (status) {
            "pending" -> "#FF9800"
            "verified" -> "#4CAF50"
            "rejected" -> "#F44336"
            else -> "#757575"
        }
    }

    fun getDurationText(): String {
        return when (packageDuration) {
            1 -> "1 Bulan"
            3 -> "3 Bulan"
            6 -> "6 Bulan"
            12 -> "1 Tahun"
            else -> "$packageDuration Bulan"
        }
    }
}
