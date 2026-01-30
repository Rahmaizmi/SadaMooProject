package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.sadamoo.admin.models.PaymentModel
import com.example.sadamoo.databinding.DialogPaymentProofBinding
import com.example.sadamoo.users.models.PaymentStatusModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentProofDialog : DialogFragment() {
    private lateinit var binding: DialogPaymentProofBinding
    private lateinit var payment: PaymentModel

    companion object {
        fun newInstance(payment: PaymentModel): PaymentProofDialog {
            val dialog = PaymentProofDialog()
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
        binding = DialogPaymentProofBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPaymentInfo()
        loadPaymentProof()

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupPaymentInfo() {
        binding.apply {
            tvPaymentCode.text = payment.paymentCode
            tvUserInfo.text = "${payment.userName} (${payment.userEmail})"
            tvPackageInfo.text = payment.packageType

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            tvAmount.text = formatter.format(payment.totalAmount).replace("IDR", "Rp")

            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            tvSubmittedDate.text = "Submitted: ${dateFormat.format(payment.transactionDate)}"

            tvStatus.text = payment.status.uppercase()
            when (payment.status) {
                "pending" -> tvStatus.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
                "verified" -> tvStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                "rejected" -> tvStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
            }

            if (payment.notes.isNotEmpty()) {
                tvNotes.text = payment.notes
                tvNotes.visibility = View.VISIBLE
            } else {
                tvNotes.visibility = View.GONE
            }
        }
    }

    private fun loadPaymentProof() {
        if (payment.paymentProofUrl?.isNotEmpty() == true) {
            binding.progressBar.visibility = View.VISIBLE

            Glide.with(this)
                .load(payment.paymentProofUrl)
                .into(binding.ivPaymentProof)
                .apply {
                    binding.progressBar.visibility = View.GONE
                }
        } else {
            binding.tvNoProof.visibility = View.VISIBLE
            binding.ivPaymentProof.visibility = View.GONE
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
