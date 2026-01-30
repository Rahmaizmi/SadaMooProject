package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.admin.models.PaymentModel
import com.example.sadamoo.databinding.DialogRefundBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RefundDialog : DialogFragment() {
    private lateinit var binding: DialogRefundBinding
    private lateinit var payment: PaymentModel
    private lateinit var onRefundProcessed: (PaymentModel) -> Unit

    private var refundAmount: Int? = 0
    private var refundReason = ""
    private var refundType = "full" // "full" or "partial"

    companion object {
        fun newInstance(
            payment: PaymentModel,
            onRefundProcessed: (PaymentModel) -> Unit
        ): RefundDialog {
            val dialog = RefundDialog()
            val args = Bundle()
            args.putParcelable("payment", payment)
            dialog.arguments = args
            dialog.onRefundProcessed = onRefundProcessed
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        payment = arguments?.getParcelable("payment") ?: return
        refundAmount = payment.totalAmount ?: payment.originalAmount ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogRefundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPaymentInfo()
        setupRefundOptions()
        setupButtons()
    }

    private fun setupPaymentInfo() {
        binding.apply {
            tvPaymentId.text = payment.id
            tvUserName.text = payment.userName
            tvPackageType.text = payment.packageType

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            tvOriginalAmount.text = formatter.format(payment.totalAmount).replace("IDR", "Rp")

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            tvPaymentDate.text = dateFormat.format(payment.transactionDate)

            // Set max refund amount
            etRefundAmount.setText(payment.totalAmount.toString())
            tvMaxRefund.text = "Max: ${formatter.format(payment.totalAmount).replace("IDR", "Rp")}"
        }
    }

    private fun setupRefundOptions() {
        // Set default to full refund
        setRefundType("full")

        binding.apply {
            // Refund type selection
            btnFullRefund.setOnClickListener { setRefundType("full") }
            btnPartialRefund.setOnClickListener { setRefundType("partial") }

            // Refund reason selection
            btnReasonRequest.setOnClickListener { setRefundReason("User requested refund") }
            btnReasonTechnical.setOnClickListener { setRefundReason("Technical issues") }
            btnReasonDuplicate.setOnClickListener { setRefundReason("Duplicate payment") }
            btnReasonFraud.setOnClickListener { setRefundReason("Fraudulent transaction") }
            btnReasonOther.setOnClickListener { setRefundReason("Other") }

            // Custom amount input listener
            etRefundAmount.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateRefundAmount()
                }
            }
        }
    }

    private fun setRefundType(type: String) {
        refundType = type

        // Reset button styles
        binding.btnFullRefund.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.refund_type_inactive)
        binding.btnPartialRefund.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.refund_type_inactive)

        when (type) {
            "full" -> {
                binding.btnFullRefund.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.refund_type_active)
                binding.etRefundAmount.setText(payment.totalAmount.toString())
                binding.etRefundAmount.isEnabled = false
                refundAmount = payment.totalAmount
            }
            "partial" -> {
                binding.btnPartialRefund.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.refund_type_active)
                binding.etRefundAmount.isEnabled = true
                binding.etRefundAmount.requestFocus()
            }
        }

        updateRefundPreview()
    }

    private fun setRefundReason(reason: String) {
        refundReason = reason

        // Reset all reason buttons
        binding.btnReasonRequest.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)
        binding.btnReasonTechnical.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)
        binding.btnReasonDuplicate.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)
        binding.btnReasonFraud.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)
        binding.btnReasonOther.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_inactive)

        // Set active reason button
        when (reason) {
            "User requested refund" -> binding.btnReasonRequest.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
            "Technical issues" -> binding.btnReasonTechnical.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
            "Duplicate payment" -> binding.btnReasonDuplicate.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
            "Fraudulent transaction" -> binding.btnReasonFraud.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
            "Other" -> {
                binding.btnReasonOther.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.reason_button_active)
                binding.etCustomReason.visibility = View.VISIBLE
                binding.etCustomReason.requestFocus()
            }
        }

        if (reason != "Other") {
            binding.etCustomReason.visibility = View.GONE
        }

        updateRefundPreview()
    }

    private fun validateRefundAmount() {
        val total = payment.totalAmount ?: payment.originalAmount ?: 0
        val amountText = binding.etRefundAmount.text.toString()
        if (amountText.isNotEmpty()) {
            val amount = amountText.toIntOrNull() ?: 0
            when {
                amount > total -> {
                    binding.etRefundAmount.error = "Amount cannot exceed total payment"
                    binding.etRefundAmount.setText(total.toString())
                    refundAmount = total
                }

                amount <= 0 -> {
                    binding.etRefundAmount.error = "Amount must be greater than 0"
                    binding.etRefundAmount.setText("1000")
                    refundAmount = 1000
                }

                else -> refundAmount = amount
            }
        }
        updateRefundPreview()
    }

    private fun updateRefundPreview() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val refundAmountText = formatter.format(refundAmount).replace("IDR", "Rp")

        val reasonText = if (refundReason == "Other" && binding.etCustomReason.text.isNotEmpty()) {
            binding.etCustomReason.text.toString()
        } else {
            refundReason
        }

        val preview = """
            Refund Amount: $refundAmountText
            Refund Type: ${refundType.uppercase()}
            Reason: $reasonText
            
            This action cannot be undone.
        """.trimIndent()

        binding.tvRefundPreview.text = preview

        // Enable/disable process button
        binding.btnProcessRefund.isEnabled = refundReason.isNotEmpty() && refundAmount!! > 0
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnProcessRefund.setOnClickListener {
            processRefund()
        }
    }

    private fun processRefund() {
        if (refundReason.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a refund reason", Toast.LENGTH_SHORT).show()
            return
        }

        if (refundAmount!! <= 0) {
            Toast.makeText(requireContext(), "Invalid refund amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Get final reason
        val finalReason = if (refundReason == "Other" && binding.etCustomReason.text.isNotEmpty()) {
            binding.etCustomReason.text.toString()
        } else {
            refundReason
        }

        // Show confirmation dialog
        showRefundConfirmation(finalReason)
    }

    private fun showRefundConfirmation(reason: String) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val refundAmountText = formatter.format(refundAmount).replace("IDR", "Rp")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Refund")
            .setMessage("""
                Are you sure you want to process this refund?
                
                User: ${payment.userName}
                Amount: $refundAmountText
                Reason: $reason
                
                This action cannot be undone.
            """.trimIndent())
            .setPositiveButton("Process Refund") { _, _ ->
                executeRefund(reason)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeRefund(reason: String) {
        // Disable button to prevent double processing
        binding.btnProcessRefund.isEnabled = false
        binding.btnProcessRefund.text = "Processing..."

        // Simulate refund processing delay
        binding.root.postDelayed({
            // Update payment model
            payment.status = "refunded"
            payment.notes = "Refunded: $reason (Amount: ${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(refundAmount).replace("IDR", "Rp")})"

            // Call callback
            onRefundProcessed(payment)

            Toast.makeText(requireContext(), "Refund processed successfully!", Toast.LENGTH_LONG).show()
            dismiss()

        }, 2000) // 2 second delay to simulate processing
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
