package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.admin.models.PaymentModel
import com.example.sadamoo.databinding.DialogRejectPaymentBinding

class RejectPaymentDialog : DialogFragment() {
    private lateinit var binding: DialogRejectPaymentBinding
    private lateinit var payment: PaymentModel
    private lateinit var onPaymentRejected: (String) -> Unit

    private var selectedReason = ""

    companion object {
        fun newInstance(
            payment: PaymentModel,
            onPaymentRejected: (String) -> Unit
        ): RejectPaymentDialog {
            val dialog = RejectPaymentDialog()
            val args = Bundle()
            args.putParcelable("payment", payment)
            dialog.arguments = args
            dialog.onPaymentRejected = onPaymentRejected
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
        binding = DialogRejectPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPaymentInfo()
        setupReasonSelection()
        setupButtons()
    }

    private fun setupPaymentInfo() {
        binding.apply {
            tvPaymentCode.text = payment.paymentCode
            tvUserName.text = payment.userName
            tvPackageInfo.text = "${payment.packageType} - ${payment.totalAmount}"
        }
    }

    private fun setupReasonSelection() {
        binding.apply {
            // Set default reason
            setReason("Bukti pembayaran tidak valid")

            btnReasonInvalid.setOnClickListener { setReason("Bukti pembayaran tidak valid") }
            btnReasonIncomplete.setOnClickListener { setReason("Informasi pembayaran tidak lengkap") }
            btnReasonAmount.setOnClickListener { setReason("Jumlah pembayaran tidak sesuai") }
            btnReasonFake.setOnClickListener { setReason("Bukti pembayaran palsu/manipulasi") }
            btnReasonExpired.setOnClickListener { setReason("Bukti pembayaran sudah kadaluarsa") }
            btnReasonOther.setOnClickListener { setReason("other") }
        }
    }

    private fun setReason(reason: String) {
        selectedReason = reason

        // Reset all buttons
        binding.btnReasonInvalid.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)
        binding.btnReasonIncomplete.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)
        binding.btnReasonAmount.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)
        binding.btnReasonFake.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)
        binding.btnReasonExpired.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)
        binding.btnReasonOther.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)

        // Set active button
        when (reason) {
            "Bukti pembayaran tidak valid" -> binding.btnReasonInvalid.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
            "Informasi pembayaran tidak lengkap" -> binding.btnReasonIncomplete.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
            "Jumlah pembayaran tidak sesuai" -> binding.btnReasonAmount.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
            "Bukti pembayaran palsu/manipulasi" -> binding.btnReasonFake.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
            "Bukti pembayaran sudah kadaluarsa" -> binding.btnReasonExpired.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
            "other" -> {
                binding.btnReasonOther.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
                binding.etCustomReason.visibility = View.VISIBLE
                binding.etCustomReason.requestFocus()
            }
        }

        if (reason != "other") {
            binding.etCustomReason.visibility = View.GONE
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnRejectPayment.setOnClickListener {
            rejectPayment()
        }
    }

    private fun rejectPayment() {
        val finalReason = if (selectedReason == "other") {
            val customReason = binding.etCustomReason.text.toString().trim()
            if (customReason.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter rejection reason", Toast.LENGTH_SHORT).show()
                return
            }
            customReason
        } else {
            selectedReason
        }

        if (finalReason.isEmpty()) {
            Toast.makeText(requireContext(), "Please select rejection reason", Toast.LENGTH_SHORT).show()
            return
        }

        onPaymentRejected(finalReason)
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
