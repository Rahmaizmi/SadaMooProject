package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.admin.models.PaymentModel
import com.example.sadamoo.databinding.DialogPaymentDetailBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentDetailDialog : DialogFragment() {
    private lateinit var binding: DialogPaymentDetailBinding
    private lateinit var payment: PaymentModel

    companion object {
        fun newInstance(payment: PaymentModel): PaymentDetailDialog {
            val dialog = PaymentDetailDialog()
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
        binding = DialogPaymentDetailBinding.inflate(inflater, container, false)
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
            tvPaymentId.text = payment.paymentCode
            tvUserName.text = payment.userName
            tvUserEmail.text = payment.userEmail
            tvPackageType.text = payment.packageType
            tvPaymentMethod.text = payment.paymentMethod
            tvPaymentCode.text = payment.paymentCode
            tvStatus.text = payment.status.uppercase()

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            // Format amount
            val total = payment.totalAmount ?: payment.originalAmount ?: 0
            tvAmount.text = formatter.format(total).replace("IDR", "Rp")

            // Format dates
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            tvTransactionDate.text = dateFormat.format(payment.transactionDate)

            if (payment.verifiedAt != null) {
                tvVerifiedDate.text = dateFormat.format(payment.verifiedAt)
                tvVerifiedDate.visibility = View.VISIBLE
                layoutVerifiedDate.visibility = View.VISIBLE
            } else {
                layoutVerifiedDate.visibility = View.GONE
            }

            // Notes
            if (payment.notes.isNotEmpty()) {
                tvNotes.text = payment.notes
                tvNotes.visibility = View.VISIBLE
                layoutNotes.visibility = View.VISIBLE
            } else {
                layoutNotes.visibility = View.GONE
            }

            // Status color
            when (payment.status) {
                "pending" -> tvStatus.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
                "completed" -> tvStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                "failed" -> tvStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                "refunded" -> tvStatus.setTextColor(requireContext().getColor(android.R.color.holo_purple))
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
