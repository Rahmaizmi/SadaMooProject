package com.example.sadamoo.admin.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.R
import com.example.sadamoo.admin.models.PaymentModel
import com.example.sadamoo.databinding.ItemPaymentBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentAdapter(
    private val payments: List<PaymentModel>,
    private val onPaymentClick: (PaymentModel) -> Unit,
    private val onVerifyPayment: (PaymentModel) -> Unit,
    private val onRejectPayment: (PaymentModel) -> Unit,
    private val onRefundPayment: (PaymentModel) -> Unit,
    private val onViewPaymentProof: (PaymentModel) -> Unit
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(private val binding: ItemPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: PaymentModel) {
            binding.apply {
                tvPaymentId.text = payment.paymentCode
                tvUserName.text = payment.userName
                tvUserEmail.text = payment.userEmail
                tvPackageType.text = payment.packageType
                tvPaymentMethod.text = payment.paymentMethod
                tvPaymentCode.text = "Code: ${payment.paymentCode}"

                // Format amount
                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                val amountToShow = payment.totalAmount ?: payment.originalAmount ?: 0
                tvAmount.text = formatter.format(amountToShow).replace("IDR", "Rp")


                // Format date
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                tvTransactionDate.text = dateFormat.format(payment.transactionDate)

                // Set status
                setupPaymentStatus(payment)

                // Set verified date
                if (payment.verifiedAt != null) {
                    tvVerifiedDate.text = "Verified: ${dateFormat.format(payment.verifiedAt)}"
                    tvVerifiedDate.visibility = View.VISIBLE
                } else {
                    tvVerifiedDate.visibility = View.GONE
                }

                // Setup action buttons
                setupActionButtons(payment)

                // Click listeners
                root.setOnClickListener { onPaymentClick(payment) }
            }
        }

        private fun setupActionButtons(payment: PaymentModel) {
            binding.apply {
                when (payment.status) {
                    "pending" -> {
                        btnVerify.visibility = View.VISIBLE
                        btnReject.visibility = View.VISIBLE
                        btnRefund.visibility = View.GONE
                        btnViewProof.visibility = View.VISIBLE

                        btnVerify.setOnClickListener { onVerifyPayment(payment) }
                        btnReject.setOnClickListener { onRejectPayment(payment) }
                        btnViewProof.setOnClickListener { onViewPaymentProof(payment) }
                    }
                    "completed" -> {
                        btnVerify.visibility = View.GONE
                        btnReject.visibility = View.GONE
                        btnRefund.visibility = View.VISIBLE
                        btnViewProof.visibility = View.VISIBLE

                        btnRefund.setOnClickListener { onRefundPayment(payment) }
                        btnViewProof.setOnClickListener { onViewPaymentProof(payment) }
                    }
                    "rejected" -> {
                        btnVerify.visibility = View.GONE
                        btnReject.visibility = View.GONE
                        btnRefund.visibility = View.GONE
                        btnViewProof.visibility = View.VISIBLE

                        btnViewProof.setOnClickListener { onViewPaymentProof(payment) }
                    }
                }
            }
        }

        private fun setupPaymentStatus(payment: PaymentModel) {
            binding.apply {
                when (payment.status) {
                    "pending" -> {
                        tvStatus.text = "PENDING"
                        tvStatus.background = itemView.context.getDrawable(R.drawable.status_pending)
                        tvStatus.setTextColor(Color.parseColor("#FF9800"))
                    }
                    "completed" -> {
                        tvStatus.text = "COMPLETED"
                        tvStatus.background = itemView.context.getDrawable(R.drawable.status_completed)
                        tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                    }
                    "failed" -> {
                        tvStatus.text = "FAILED"
                        tvStatus.background = itemView.context.getDrawable(R.drawable.status_failed)
                        tvStatus.setTextColor(Color.parseColor("#F44336"))
                    }
                    "refunded" -> {
                        tvStatus.text = "REFUNDED"
                        tvStatus.background = itemView.context.getDrawable(R.drawable.status_refunded)
                        tvStatus.setTextColor(Color.parseColor("#9C27B0"))
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(payments[position])
    }

    override fun getItemCount(): Int = payments.size
}
