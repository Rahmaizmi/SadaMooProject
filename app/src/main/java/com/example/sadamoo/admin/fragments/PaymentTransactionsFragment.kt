package com.example.sadamoo.admin.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.R
import com.example.sadamoo.admin.adapters.PaymentAdapter
import com.example.sadamoo.admin.dialogs.PaymentDetailDialog
import com.example.sadamoo.admin.dialogs.PaymentProofDialog
import com.example.sadamoo.admin.dialogs.RefundDialog
import com.example.sadamoo.admin.dialogs.RejectPaymentDialog
import com.example.sadamoo.admin.models.PaymentModel
import com.example.sadamoo.databinding.FragmentPaymentTransactionsBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

class PaymentTransactionsFragment : Fragment() {
    private var _binding: FragmentPaymentTransactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var paymentAdapter: PaymentAdapter
    private val allPayments = mutableListOf<PaymentModel>()
    private val filteredPayments = mutableListOf<PaymentModel>()
    private var currentFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupButtons()
        loadPayments()
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter(
            payments = filteredPayments,
            onPaymentClick = { showPaymentDetail(it) },
            onVerifyPayment = { verifyPayment(it) },
            onRejectPayment = { rejectPayment(it) },
            onRefundPayment = { processRefund(it) },
            onViewPaymentProof = { viewPaymentProof(it) }
        )

        binding.rvPayments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = paymentAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                filterPayments(query, currentFilter)
            }
        })
    }

    private fun setupFilters() {
        setActiveFilter("all")

        binding.btnFilterAll.setOnClickListener {
            setActiveFilter("all")
            filterPayments(binding.etSearch.text.toString(), "all")
        }

        binding.btnFilterPending.setOnClickListener {
            setActiveFilter("pending")
            filterPayments(binding.etSearch.text.toString(), "pending")
        }

        binding.btnFilterCompleted.setOnClickListener {
            setActiveFilter("completed")
            filterPayments(binding.etSearch.text.toString(), "completed")
        }

        binding.btnFilterFailed.setOnClickListener {
            setActiveFilter("failed")
            filterPayments(binding.etSearch.text.toString(), "failed")
        }
    }

    private fun setActiveFilter(filter: String) {
        currentFilter = filter

        val inactiveBg = R.drawable.filter_button_inactive
        val activeBg = R.drawable.filter_button_active
        val inactiveTextColor = requireContext().getColor(R.color.blue_primary)
        val activeTextColor = requireContext().getColor(android.R.color.white)

        // Reset semua button
        listOf(
            binding.btnFilterAll,
            binding.btnFilterPending,
            binding.btnFilterCompleted,
            binding.btnFilterFailed
        ).forEach {
            it.background = requireContext().getDrawable(inactiveBg)
            it.setTextColor(inactiveTextColor)
        }

        // Aktifkan button sesuai filter
        when (filter) {
            "all" -> {
                binding.btnFilterAll.background = requireContext().getDrawable(activeBg)
                binding.btnFilterAll.setTextColor(activeTextColor)
            }
            "pending" -> {
                binding.btnFilterPending.background = requireContext().getDrawable(activeBg)
                binding.btnFilterPending.setTextColor(activeTextColor)
            }
            "completed" -> {
                binding.btnFilterCompleted.background = requireContext().getDrawable(activeBg)
                binding.btnFilterCompleted.setTextColor(activeTextColor)
            }
            "failed" -> {
                binding.btnFilterFailed.background = requireContext().getDrawable(activeBg)
                binding.btnFilterFailed.setTextColor(activeTextColor)
            }
        }
    }


    private fun loadPayments() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val snapshot = firestore.collection("payments")
                    .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                allPayments.clear()

                for (doc in snapshot.documents) {
                    val payment = PaymentModel(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Tidak diketahui",
                        userEmail = doc.getString("userEmail") ?: "-",
                        packageType = doc.getString("packageType") ?: "-",
                        status = doc.getString("status") ?: "pending",
                        paymentMethod = doc.getString("paymentMethod") ?: "Bank Transfer",
                        transactionDate = doc.getTimestamp("transactionDate")?.toDate() ?: Date(),
                        verifiedAt = doc.getTimestamp("verifiedAt")?.toDate(),
                        paymentCode = doc.getString("paymentCode") ?: "-",
                        notes = doc.getString("notes") ?: "-",
                        paymentProofUrl = doc.getString("paymentProofUrl"),

                        // 🔥 Tambahan field baru biar sesuai model
                        originalAmount = doc.getLong("originalAmount")?.toInt(),
                        adminFee = doc.getLong("adminFee")?.toInt(),
                        totalAmount = doc.getLong("totalAmount")?.toInt(),
                        bankAccount = doc.getString("bankAccount"),

                        submittedAt = doc.getTimestamp("submittedAt")?.toDate(),
                        verifiedBy = doc.getString("verifiedBy"),
                        packageDuration = doc.getLong("packageDuration")?.toInt(),
                        subscriptionStartDate = doc.getTimestamp("subscriptionStartDate")?.toDate(),
                        subscriptionEndDate = doc.getTimestamp("subscriptionEndDate")?.toDate()
                    )

                    allPayments.add(payment)
                }


                allPayments.sortByDescending { it.transactionDate }
                filteredPayments.clear()
                filteredPayments.addAll(allPayments)
                paymentAdapter.notifyDataSetChanged()

                updatePaymentCount()
                loadPaymentStats()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal memuat pembayaran: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadPaymentStats() {
        lifecycleScope.launch {
            try {
                val totalRevenue = allPayments.filter { it.status == "completed" }
                    .sumOf { it.totalAmount ?: 0 }
                val pending = allPayments.count { it.status == "pending" }
                val completed = allPayments.count { it.status == "completed" }
                val failed = allPayments.count { it.status == "failed" || it.status == "rejected" }

                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                binding.tvTotalRevenue.text =
                    formatter.format(totalRevenue).replace("IDR", "Rp")
                binding.tvPendingPayments.text = pending.toString()
                binding.tvCompletedPayments.text = completed.toString()
                binding.tvFailedPayments.text = failed.toString()
            } catch (_: Exception) {
            }
        }
    }

    private fun rejectPayment(payment: PaymentModel) {
        val dialog = RejectPaymentDialog.newInstance(payment) { reason ->
            performRejectPayment(payment, reason)
        }
        dialog.show(parentFragmentManager, "RejectPaymentDialog")
    }

    private fun performRejectPayment(payment: PaymentModel, reason: String) {
        lifecycleScope.launch {
            try {
                firestore.collection("payments").document(payment.id)
                    .update(
                        mapOf(
                            "status" to "rejected",
                            "verifiedAt" to com.google.firebase.Timestamp.now(),
                            "verifiedBy" to "admin",
                            "notes" to "Payment rejected: $reason"
                        )
                    ).await()

                Toast.makeText(requireContext(), "Payment rejected!", Toast.LENGTH_SHORT).show()
                loadPayments()
                loadPaymentStats()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun viewPaymentProof(payment: PaymentModel) {
        PaymentProofDialog.newInstance(payment)
            .show(parentFragmentManager, "PaymentProofDialog")
    }

    private fun verifyPayment(payment: PaymentModel) {
        lifecycleScope.launch {
            try {
                firestore.collection("payments").document(payment.id)
                    .update(
                        mapOf(
                            "status" to "completed",
                            "verifiedAt" to com.google.firebase.Timestamp.now(),
                            "verifiedBy" to "admin",
                            "notes" to "Payment verified by admin"
                        )
                    ).await()

                updateUserSubscriptionAfterPayment(payment)
                Toast.makeText(requireContext(), "Payment verified successfully!", Toast.LENGTH_SHORT).show()

                loadPayments()
                loadPaymentStats()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error verifying: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun updateUserSubscriptionAfterPayment(payment: PaymentModel) {
        try {
            val calendar = Calendar.getInstance()
            when (payment.packageType) {
                "Basic" -> calendar.add(Calendar.MONTH, 1)
                "Standard" -> calendar.add(Calendar.MONTH, 3)
                "Premium" -> calendar.add(Calendar.MONTH, 6)
                "Ultimate" -> calendar.add(Calendar.YEAR, 1)
            }

            firestore.collection("users").document(payment.userId)
                .update(
                    mapOf(
                        "subscriptionStatus" to "active",
                        "subscriptionType" to payment.packageType,
                        "subscriptionEndDate" to com.google.firebase.Timestamp(calendar.time),
                        "paymentVerifiedAt" to com.google.firebase.Timestamp.now()
                    )
                ).await()
        } catch (_: Exception) {
        }
    }

    private fun processRefund(payment: PaymentModel) {
        val dialog = RefundDialog.newInstance(payment) {
            it.status = "refunded"
            Toast.makeText(requireContext(), "Refund processed!", Toast.LENGTH_SHORT).show()
            loadPayments()
            loadPaymentStats()
        }
        dialog.show(parentFragmentManager, "RefundDialog")
    }

    private fun filterPayments(query: String, filter: String) {
        var filtered = allPayments

        when (filter) {
            "pending" -> filtered = filtered.filter { it.status == "pending" }.toMutableList()
            "completed" -> filtered = filtered.filter { it.status == "completed" }.toMutableList()
            "failed" -> filtered = filtered.filter { it.status == "failed" || it.status == "rejected" }.toMutableList()
        }

        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.userName.lowercase().contains(query) ||
                        it.userEmail.lowercase().contains(query) ||
                        it.paymentCode.lowercase().contains(query)
            }.toMutableList()
        }

        filteredPayments.clear()
        filteredPayments.addAll(filtered)
        paymentAdapter.notifyDataSetChanged()
        updatePaymentCount()
    }

    private fun updatePaymentCount() {
        binding.tvPaymentCount.text = "${filteredPayments.size} pembayaran ditemukan"
    }

    private fun showPaymentDetail(payment: PaymentModel) {
        PaymentDetailDialog.newInstance(payment)
            .show(parentFragmentManager, "PaymentDetailDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
