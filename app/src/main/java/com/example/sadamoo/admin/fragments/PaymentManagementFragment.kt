package com.example.sadamoo.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sadamoo.databinding.FragmentPaymentManagementBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

class PaymentManagementFragment : Fragment() {
    private var _binding: FragmentPaymentManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

        setupButtons()
        loadPaymentOverview()
    }

    private fun setupButtons() {
        binding.btnManagePayments.setOnClickListener {
            // Navigate to Payment Transactions Fragment
            navigateToFragment(PaymentTransactionsFragment())
        }

        binding.btnManagePackages.setOnClickListener {
            // Navigate to Package Management Fragment
            navigateToFragment(PackageManagementFragment())
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadPaymentOverview() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                loadRevenueStats()
                loadPackageStats()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadRevenueStats() {
        val calendar = Calendar.getInstance()
        val startOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val paymentsSnapshot = firestore.collection("payments")
            .whereGreaterThanOrEqualTo("transactionDate", Timestamp(startOfMonth))
            .get().await()

        val totalRevenue = paymentsSnapshot.documents
            .filter {
                val status = it.getString("status")
                status == "completed" || status == "verified"
            }
            .sumOf { it.getLong("totalAmount")?.toInt() ?: 0 }

        val monthlyRevenue = totalRevenue // semua transaksi bulan ini
        val activeSubscriptions = firestore.collection("users")
            .whereEqualTo("subscriptionStatus", "active")
            .get().await().size()
        val pendingPayments =
            paymentsSnapshot.documents.count { it.getString("status") == "pending" }

        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        binding.tvTotalRevenue.text = formatter.format(totalRevenue).replace("IDR", "Rp")
        binding.tvMonthlyRevenue.text = formatter.format(monthlyRevenue).replace("IDR", "Rp")
        binding.tvActiveSubscriptions.text = activeSubscriptions.toString()
        binding.tvPendingPayments.text = pendingPayments.toString()
    }

    private suspend fun loadPackageStats() {
        val packagesSnapshot = firestore.collection("subscription_packages").get().await()

        val packageStats = mutableListOf<Map<String, Any>>()

        for (doc in packagesSnapshot.documents) {
            val name = doc.getString("name") ?: "Tanpa Nama"
            val price = doc.getLong("price")?.toInt() ?: 0

            val subscribers = firestore.collection("users")
                .whereEqualTo("subscriptionType", name)
                .whereEqualTo("subscriptionStatus", "active")
                .get().await().size()

            val revenue = subscribers * price

            packageStats.add(
                mapOf(
                    "name" to name,
                    "subscribers" to subscribers,
                    "revenue" to revenue
                )
            )
        }

        val mostPopular = packageStats.maxByOrNull { it["subscribers"] as Int }
        val highestRevenue = packageStats.maxByOrNull { it["revenue"] as Int }

        binding.tvMostPopularPackage.text = mostPopular?.get("name") as? String ?: "N/A"
        binding.tvHighestRevenuePackage.text = highestRevenue?.get("name") as? String ?: "N/A"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
