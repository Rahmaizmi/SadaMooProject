package com.example.sadamoo.admin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class PaymentModel(
    val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val packageType: String,
    var status: String,
    val paymentMethod: String,
    val transactionDate: Date,
    var verifiedAt: Date?,
    val paymentCode: String,
    var notes: String,
    val paymentProofUrl: String?,
    val originalAmount: Int?,
    val adminFee: Int?,
    val totalAmount: Int?,
    val bankAccount: String?,
    val submittedAt: Date?,
    val verifiedBy: String?,
    val packageDuration: Int?,
    val subscriptionStartDate: Date?,
    val subscriptionEndDate: Date?
) : Parcelable
