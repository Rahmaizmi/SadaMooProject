package com.example.sadamoo.users.fragments

import android.graphics.BitmapFactory
import android.util.Base64
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.sadamoo.LoginActivity
import com.example.sadamoo.R
import com.example.sadamoo.databinding.FragmentProfileBinding
import com.example.sadamoo.users.EditProfileActivity
import com.example.sadamoo.users.HelpSupportActivity
import com.example.sadamoo.users.SettingsActivity
import com.example.sadamoo.users.dialogs.UpgradeDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop



class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userDocumentListener: ListenerRegistration? = null
    private var trialCountdownHandler: Handler? = null
    private var trialCountdownRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserProfile()
        setupClickListeners()
        startTrialCountdown()
    }

    private fun startTrialCountdown() {
        trialCountdownHandler = Handler(Looper.getMainLooper())
        trialCountdownRunnable = object : Runnable {
            override fun run() {
                updateTrialCountdown()
                trialCountdownHandler?.postDelayed(this, 60000)
            }
        }
        trialCountdownHandler?.post(trialCountdownRunnable!!)
    }

    private fun updateTrialCountdown() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val subscriptionStatus = document.getString("subscriptionStatus") ?: "trial"
                        val trialStartDate = document.getTimestamp("trialStartDate")

                        if (subscriptionStatus == "trial" && trialStartDate != null) {
                            val currentTime = System.currentTimeMillis()
                            val trialStart = trialStartDate.toDate().time
                            val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
                            val timeLeft = (trialStart + sevenDaysInMillis) - currentTime

                            if (timeLeft > 0) {
                                binding.tvTrialDaysLeft.text = calculateDetailedTimeLeft(trialStartDate)
                            } else {
                                firestore.collection("users").document(currentUser.uid)
                                    .update("subscriptionStatus", "expired")
                                    .addOnSuccessListener {
                                        binding.tvTrialDaysLeft.text = "Trial berakhir"
                                    }
                            }
                        }
                    }
                }
        }
    }

    private fun calculateDetailedTimeLeft(trialStartDate: com.google.firebase.Timestamp): String {
        val currentTime = System.currentTimeMillis()
        val trialStart = trialStartDate.toDate().time
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
        val timeLeft = (trialStart + sevenDaysInMillis) - currentTime

        return if (timeLeft > 0) {
            val days = timeLeft / (24 * 60 * 60 * 1000L)
            val hours = (timeLeft % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)
            val minutes = (timeLeft % (60 * 60 * 1000L)) / (60 * 1000L)

            when {
                days > 0 -> "$days hari ${hours}j ${minutes}m tersisa"
                hours > 0 -> "${hours}j ${minutes}m tersisa"
                minutes > 0 -> "${minutes}m tersisa"
                else -> "Trial berakhir"
            }
        } else {
            "Trial berakhir"
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userDocumentListener = firestore.collection("users").document(currentUser.uid)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        binding.tvUserEmail.text = currentUser.email

                        val userName = document.getString("name") ?: "User"
                        val subscriptionStatus = document.getString("subscriptionStatus") ?: "trial"
                        val trialStartDate = document.getTimestamp("trialStartDate")

                        binding.tvUserName.text = userName

                        val photoBase64 = document.getString("photoBase64")
                        if (!photoBase64.isNullOrEmpty()) {
                            try {
                                val imageBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                                Glide.with(requireContext())
                                    .load(bitmap)
                                    .transform(CircleCrop())
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(binding.ivProfileAvatar)
                                binding.ivProfileAvatar.imageTintList = null
                            } catch (e: Exception) {
                                binding.ivProfileAvatar.setImageResource(R.drawable.ic_profile)
                                binding.ivProfileAvatar.imageTintList = android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#4A90E2")
                                )
                            }
                        } else {
                            binding.ivProfileAvatar.setImageResource(R.drawable.ic_profile)
                            binding.ivProfileAvatar.imageTintList = android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#4A90E2")
                            )
                        }
                        updateSubscriptionStatus(subscriptionStatus, trialStartDate)
                        loadUserStatistics(currentUser.uid)
                    }
                }
        }
    }

    private fun updateSubscriptionStatus(
        status: String,
        trialStartDate: com.google.firebase.Timestamp?
    ) {
        when (status) {
            "trial" -> {
                if (trialStartDate != null) {
                    val daysLeft = calculateTrialDaysLeft(trialStartDate)

                    if (daysLeft > 0) {
                        binding.tvSubscriptionStatus.text = "Trial"
                        binding.tvSubscriptionStatus.background =
                            requireContext().getDrawable(R.drawable.subscription_badge_trial)

                        binding.tvTrialDaysLeft.text = calculateDetailedTimeLeft(trialStartDate)
                        binding.tvTrialDaysLeft.setTextColor(
                            when {
                                daysLeft > 2 -> Color.parseColor("#FF5722")
                                daysLeft > 0 -> Color.parseColor("#F44336")
                                else -> Color.parseColor("#D32F2F")
                            }
                        )

                        binding.cardSubscription.visibility = View.VISIBLE
                    } else {
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            firestore.collection("users").document(currentUser.uid)
                                .update("subscriptionStatus", "expired")
                        }

                        binding.tvSubscriptionStatus.text = "Berakhir"
                        binding.tvSubscriptionStatus.background =
                            requireContext().getDrawable(R.drawable.subscription_badge_expired)
                        binding.tvTrialDaysLeft.text = "Trial berakhir"
                        binding.tvTrialDaysLeft.setTextColor(Color.parseColor("#F44336"))
                        binding.cardSubscription.visibility = View.VISIBLE
                    }
                }
            }

            "active" -> {
                binding.tvSubscriptionStatus.text = "Premium"
                binding.tvSubscriptionStatus.background =
                    requireContext().getDrawable(R.drawable.subscription_badge_premium)
                binding.tvTrialDaysLeft.text = "Aktif"
                binding.tvTrialDaysLeft.setTextColor(Color.parseColor("#4CAF50"))

                binding.cardSubscription.visibility = View.GONE
            }

            "expired" -> {
                binding.tvSubscriptionStatus.text = "Berakhir"
                binding.tvSubscriptionStatus.background =
                    requireContext().getDrawable(R.drawable.subscription_badge_expired)
                binding.tvTrialDaysLeft.text = "Perlu diperpanjang"
                binding.tvTrialDaysLeft.setTextColor(Color.parseColor("#F44336"))

                binding.cardSubscription.visibility = View.VISIBLE
            }
        }
    }

    private fun calculateTrialDaysLeft(trialStartDate: com.google.firebase.Timestamp): Int {
        val currentTime = System.currentTimeMillis()
        val trialStart = trialStartDate.toDate().time
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
        val timeLeft = (trialStart + sevenDaysInMillis) - currentTime

        return if (timeLeft > 0) (timeLeft / (24 * 60 * 60 * 1000L)).toInt() else 0
    }

    private fun loadUserStatistics(userId: String) {
//        createSampleScanHistory(userId)

        firestore.collection("scan_history")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    binding.tvTotalScans.text = "0"
                    binding.tvHealthyCattle.text = "0"
                    binding.tvDiseasedCattle.text = "0"
                    return@addSnapshotListener
                }

                if (documents != null) {
                    val totalScans = documents.size()
                    var healthyCattle = 0
                    var diseasedCattle = 0

                    for (document in documents) {

                        val isHealthy = document.getBoolean("isHealthy") ?: false

                        if (isHealthy) {
                            healthyCattle++
                        } else {
                            diseasedCattle++
                        }
                    }


                    binding.tvTotalScans.text = totalScans.toString()
                    binding.tvHealthyCattle.text = healthyCattle.toString()
                    binding.tvDiseasedCattle.text = diseasedCattle.toString()
                } else {
                    binding.tvTotalScans.text = "0"
                    binding.tvHealthyCattle.text = "0"
                    binding.tvDiseasedCattle.text = "0"
                }
            }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnUpgradePremium.setOnClickListener {
            showUpgradeDialog()
        }

        binding.menuEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.menuSubscription.setOnClickListener {
            showSubscriptionManagement()
        }

        binding.menuSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.menuHelp.setOnClickListener {
            startActivity(Intent(requireContext(), HelpSupportActivity::class.java))
        }

        binding.menuLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showUpgradeDialog() {
        val dialog = UpgradeDialogFragment()
        dialog.show(parentFragmentManager, "UpgradeDialog")
    }

    // Tambahkan method ini di ProfileFragment.kt setelah method showUpgradeDialog()

    private fun showSubscriptionManagement() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val subscriptionStatus = document.getString("subscriptionStatus") ?: "trial"
                        val subscriptionType = document.getString("subscriptionType") ?: ""
                        val subscriptionEndDate = document.getTimestamp("subscriptionEndDate")

                        showSubscriptionDetails(subscriptionStatus, subscriptionType, subscriptionEndDate)
                    }
                }
        }
    }

    private fun showSubscriptionDetails(status: String, type: String, endDate: com.google.firebase.Timestamp?) {
        val message = when (status) {
            "trial" -> "Anda sedang dalam masa trial 7 hari gratis."
            "active" -> {
                val endDateStr = if (endDate != null) {
                    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(endDate.toDate())
                } else "Tidak diketahui"
                "Langganan $type aktif hingga $endDateStr"
            }
            "expired" -> "Langganan Anda telah berakhir. Upgrade untuk melanjutkan akses premium."
            else -> "Status langganan tidak diketahui."
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Status Langganan")
            .setMessage(message)
            .setPositiveButton("Upgrade") { _, _ ->
                showUpgradeDialog()
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setPositiveButton("Keluar") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()

        // Clear any cached data if needed
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        // Navigate to login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()

        Toast.makeText(requireContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Hentikan countdown lebih aman
        trialCountdownRunnable?.let { runnable ->
            trialCountdownHandler?.removeCallbacks(runnable)
        }

        // Hapus listener Firestore
        userDocumentListener?.remove()

        // Null-kan binding di paling akhir
        _binding = null
    }

}

