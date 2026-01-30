package com.example.sadamoo.users.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.databinding.FragmentPaymentStatusBinding
import com.example.sadamoo.users.adapters.PaymentStatusAdapter
import com.example.sadamoo.users.dialogs.PaymentStatusDetailDialog
import com.example.sadamoo.users.models.PaymentStatusModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PaymentStatusFragment : Fragment() {
    private var _binding: FragmentPaymentStatusBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var paymentAdapter: PaymentStatusAdapter
    private val paymentList = mutableListOf<PaymentStatusModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadPaymentHistory()
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentStatusAdapter(paymentList) { payment ->
            // Fix: Use PaymentStatusModel for user side
            showPaymentDetail(payment)
        }

        binding.rvPaymentHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = paymentAdapter
        }
    }

    private fun loadPaymentHistory() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val currentUser = auth.currentUser ?: return@launch

                val paymentsSnapshot = firestore.collection("payments")
                    .whereEqualTo("userId", currentUser.uid)
                    .orderBy("submittedAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                paymentList.clear()

                for (document in paymentsSnapshot.documents) {
                    val payment = PaymentStatusModel(
                        id = document.id,
                        paymentCode = document.getString("paymentCode") ?: "",
                        packageType = document.getString("packageType") ?: "",
                        packageDuration = document.getLong("packageDuration")?.toInt() ?: 1,
                        totalAmount = document.getLong("totalAmount")?.toInt() ?: 0,
                        status = document.getString("status") ?: "pending",
                        submittedAt = document.getTimestamp("submittedAt")?.toDate(),
                        verifiedAt = document.getTimestamp("verifiedAt")?.toDate(),
                        notes = document.getString("notes") ?: "",
                        paymentProofUrl = document.getString("paymentProofUrl") ?: ""
                    )
                    paymentList.add(payment)
                }

                paymentAdapter.notifyDataSetChanged()

                if (paymentList.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvPaymentHistory.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvPaymentHistory.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                binding.tvEmptyState.text = "Error loading payment history"
                binding.tvEmptyState.visibility = View.VISIBLE
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showPaymentDetail(payment: PaymentStatusModel) {
        val dialog = PaymentStatusDetailDialog.newInstance(payment)
        dialog.show(parentFragmentManager, "PaymentStatusDetailDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
