package com.example.sadamoo.users.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.sadamoo.databinding.DialogPaymentStatusDetailBinding
import com.example.sadamoo.users.models.PaymentStatusModel
import java.text.SimpleDateFormat
import java.util.*

class PaymentStatusDetailDialog : DialogFragment() {
    private lateinit var binding: DialogPaymentStatusDetailBinding
    private lateinit var payment: PaymentStatusModel

    companion object {
        fun newInstance(payment: PaymentStatusModel): PaymentStatusDetailDialog {
            val dialog = PaymentStatusDetailDialog()
            val args = Bundle()
            args.putParcelable("payment", payment)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        payment = arguments?.getParcelable("payment") ?: return
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPaymentStatusDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPaymentDetails()

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupPaymentDetails() {
        binding.apply {
            tvPaymentCode.text = payment.paymentCode
            tvPackageInfo.text = "Paket ${payment.packageType} - ${payment.getDurationText()}"
            tvAmount.text = payment.getFormattedAmount()
            tvStatus.text = payment.getStatusText()
            tvNotes.text = payment.notes

            // Set status color
            tvStatus.setTextColor(android.graphics.Color.parseColor(payment.getStatusColor()))

            // Format dates
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))

            if (payment.submittedAt != null) {
                tvSubmittedDate.text = "Dikirim: ${dateFormat.format(payment.submittedAt)}"
            }

            if (payment.verifiedAt != null) {
                tvVerifiedDate.text = "Diverifikasi: ${dateFormat.format(payment.verifiedAt)}"
                tvVerifiedDate.visibility = View.VISIBLE
            } else {
                tvVerifiedDate.visibility = View.GONE
            }

            // Load payment proof if available
            if (payment.paymentProofUrl.isNotEmpty()) {
                Glide.with(this@PaymentStatusDetailDialog)
                    .load(payment.paymentProofUrl)
                    .into(ivPaymentProof)
                ivPaymentProof.visibility = View.VISIBLE
            } else {
                ivPaymentProof.visibility = View.GONE
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
