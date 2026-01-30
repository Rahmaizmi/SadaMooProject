package com.example.sadamoo.users.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.databinding.ItemPaymentStatusBinding
import com.example.sadamoo.users.models.PaymentStatusModel
import java.text.SimpleDateFormat
import java.util.*

class PaymentStatusAdapter(
    private val payments: List<PaymentStatusModel>,
    private val onPaymentClick: (PaymentStatusModel) -> Unit
) : RecyclerView.Adapter<PaymentStatusAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(private val binding: ItemPaymentStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: PaymentStatusModel) {
            binding.apply {
                tvPaymentCode.text = payment.paymentCode
                tvPackageInfo.text = "Paket ${payment.packageType} - ${payment.getDurationText()}"
                tvAmount.text = payment.getFormattedAmount()
                tvStatus.text = payment.getStatusText()
                tvNotes.text = payment.notes

                // Set status color
                tvStatus.setTextColor(Color.parseColor(payment.getStatusColor()))

                // Format date
                if (payment.submittedAt != null) {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                    tvSubmittedDate.text = "Dikirim: ${dateFormat.format(payment.submittedAt)}"
                }

                if (payment.verifiedAt != null) {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                    tvVerifiedDate.text = "Diverifikasi: ${dateFormat.format(payment.verifiedAt)}"
                    tvVerifiedDate.visibility = android.view.View.VISIBLE
                } else {
                    tvVerifiedDate.visibility = android.view.View.GONE
                }

                // Set status background
                when (payment.status) {
                    "pending" -> {
                        cardStatus.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                    }
                    "verified" -> {
                        cardStatus.setCardBackgroundColor(Color.parseColor("#E8F5E8"))
                    }
                    "rejected" -> {
                        cardStatus.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                    }
                }

                root.setOnClickListener { onPaymentClick(payment) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentStatusBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(payments[position])
    }

    override fun getItemCount(): Int = payments.size
}
