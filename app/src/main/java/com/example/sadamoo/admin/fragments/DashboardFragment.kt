package com.example.sadamoo.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sadamoo.databinding.FragmentDashboardBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        setupUI()
        loadDashboardData()
    }

    private fun setupUI() {
        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
        binding.tvCurrentDate.text = currentDate
        binding.tvAdminGreeting.text = "Selamat datang, Admin!"
    }

    private fun loadDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                loadUserStatistics()
                loadRevenueStatistics()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadUserStatistics() {
        try {
            val usersSnapshot = firestore.collection("users").get().await()
            val totalUsers = usersSnapshot.size()

            val trialUsers = usersSnapshot.documents.count {
                it.getString("subscriptionStatus") == "trial"
            }

            val premiumUsers = usersSnapshot.documents.count {
                it.getString("subscriptionStatus") == "active"
            }

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val newUsersToday = usersSnapshot.documents.count { document ->
                val createdAt = document.getTimestamp("createdAt")
                createdAt != null && createdAt.toDate().after(today)
            }

            binding.tvTotalUsers.text = totalUsers.toString()
            binding.tvTrialUsers.text = trialUsers.toString()
            binding.tvPremiumUsers.text = premiumUsers.toString()
            binding.tvNewUsersToday.text = newUsersToday.toString()

        } catch (e: Exception) {
            binding.tvTotalUsers.text = "0"
            binding.tvTrialUsers.text = "0"
            binding.tvPremiumUsers.text = "0"
            binding.tvNewUsersToday.text = "0"
        }
    }

    private suspend fun loadRevenueStatistics() {
        try {
            val calendar = Calendar.getInstance()
            val startOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val endOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val paymentsSnapshot = firestore.collection("payments")
                .whereGreaterThanOrEqualTo("transactionDate", Timestamp(startOfMonth))
                .whereLessThanOrEqualTo("transactionDate", Timestamp(endOfMonth))
                .get().await()

            val totalRevenue = paymentsSnapshot.documents
                .filter {
                    val status = it.getString("status")
                    status == "completed" || status == "verified"
                }
                .sumOf { it.getLong("totalAmount")?.toInt() ?: 0 }

            val activeSubscriptions = firestore.collection("users")
                .whereEqualTo("subscriptionStatus", "active")
                .get().await().size()

            val dailyRevenue = totalRevenue / maxOf(1, calendar.get(Calendar.DAY_OF_MONTH))

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvMonthlyRevenue.text = formatter.format(totalRevenue).replace("IDR", "Rp")
            binding.tvDailyRevenue.text = formatter.format(dailyRevenue).replace("IDR", "Rp")
            binding.tvActiveSubscriptions.text = activeSubscriptions.toString()

        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvMonthlyRevenue.text = "Rp 0"
            binding.tvDailyRevenue.text = "Rp 0"
            binding.tvActiveSubscriptions.text = "0"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
