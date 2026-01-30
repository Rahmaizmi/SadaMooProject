package com.example.sadamoo.users
//
//import android.content.Intent
//import android.graphics.Color
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import com.example.sadamoo.LoginActivity
//import com.example.sadamoo.R
//import com.example.sadamoo.databinding.ActivityProfileBinding
//import com.example.sadamoo.users.dialogs.UpgradeDialogFragment
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ListenerRegistration
//import java.text.SimpleDateFormat
//import java.util.*
//import com.example.sadamoo.utils.applyStatusBarPadding
//import android.view.View
//
//class ProfileActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityProfileBinding
//    private lateinit var auth: FirebaseAuth
//    private lateinit var firestore: FirebaseFirestore
//    private var userDocumentListener: ListenerRegistration? = null
//    private var trialCountdownHandler: Handler? = null
//    private var trialCountdownRunnable: Runnable? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityProfileBinding.inflate(layoutInflater)
//        binding.root.applyStatusBarPadding()
//        setContentView(binding.root)
//
//        // Initialize Firebase
//        auth = FirebaseAuth.getInstance()
//        firestore = FirebaseFirestore.getInstance()
//
//        setupBottomNavigation()
//        loadUserProfile()
//        setupClickListeners()
//        startTrialCountdown()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // Clean up listeners
//        userDocumentListener?.remove()
//        trialCountdownHandler?.removeCallbacks(trialCountdownRunnable!!)
//    }
//
//    private fun startTrialCountdown() {
//        trialCountdownHandler = Handler(Looper.getMainLooper())
//        trialCountdownRunnable = object : Runnable {
//            override fun run() {
//                updateTrialCountdown()
//                trialCountdownHandler?.postDelayed(this, 60000) // Update every minute
//            }
//        }
//        trialCountdownHandler?.post(trialCountdownRunnable!!)
//    }
//
//    private fun updateTrialCountdown() {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            firestore.collection("users").document(currentUser.uid)
//                .get()
//                .addOnSuccessListener { document ->
//                    if (document.exists()) {
//                        val subscriptionStatus = document.getString("subscriptionStatus") ?: "trial"
//                        val trialStartDate = document.getTimestamp("trialStartDate")
//
//                        if (subscriptionStatus == "trial" && trialStartDate != null) {
//                            val currentTime = System.currentTimeMillis()
//                            val trialStart = trialStartDate.toDate().time
//                            val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
//                            val timeLeft = (trialStart + sevenDaysInMillis) - currentTime
//
//                            if (timeLeft > 0) {
//                                binding.tvTrialDaysLeft.text = calculateDetailedTimeLeft(trialStartDate)
//                            } else {
//                                // Ubah Firestore ke expired
//                                firestore.collection("users").document(currentUser.uid)
//                                    .update("subscriptionStatus", "expired")
//                                    .addOnSuccessListener {
//                                        binding.tvTrialDaysLeft.text = "Trial berakhir"
//                                    }
//                            }
//                        }
//                    }
//                }
//        }
//    }
//
//
//    private fun calculateDetailedTimeLeft(trialStartDate: com.google.firebase.Timestamp): String {
//        val currentTime = System.currentTimeMillis()
//        val trialStart = trialStartDate.toDate().time
//        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
//        val timeLeft = (trialStart + sevenDaysInMillis) - currentTime
//
//        return if (timeLeft > 0) {
//            val days = timeLeft / (24 * 60 * 60 * 1000L)
//            val hours = (timeLeft % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)
//            val minutes = (timeLeft % (60 * 60 * 1000L)) / (60 * 1000L)
//
//            when {
//                days > 0 -> "$days hari ${hours}j ${minutes}m tersisa"
//                hours > 0 -> "${hours}j ${minutes}m tersisa"
//                minutes > 0 -> "${minutes}m tersisa"
//                else -> "Trial berakhir"
//            }
//        } else {
//            "Trial berakhir"
//        }
//    }
//
//    fun loadUserProfile() {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            // Setup real-time listener for user data
//            userDocumentListener = firestore.collection("users").document(currentUser.uid)
//                .addSnapshotListener { document, error ->
//                    if (error != null) {
//                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
//                        return@addSnapshotListener
//                    }
//
//                    if (document != null && document.exists()) {
//                        // Load basic user info
//                        binding.tvUserEmail.text = currentUser.email
//
//                        val userName = document.getString("name") ?: "User"
//                        val subscriptionStatus = document.getString("subscriptionStatus") ?: "trial"
//                        val trialStartDate = document.getTimestamp("trialStartDate")
//
//                        binding.tvUserName.text = userName
//                        updateSubscriptionStatus(subscriptionStatus, trialStartDate)
//                        loadUserStatistics(currentUser.uid)
//                    }
//                }
//        }
//    }
//
//    private fun updateSubscriptionStatus(
//        status: String,
//        trialStartDate: com.google.firebase.Timestamp?
//    ) {
//        when (status) {
//            "trial" -> {
//                if (trialStartDate != null) {
//                    val daysLeft = calculateTrialDaysLeft(trialStartDate)
//
//                    if (daysLeft > 0) {
//                        // Trial masih aktif
//                        binding.tvSubscriptionStatus.text = "Trial"
//                        binding.tvSubscriptionStatus.background =
//                            getDrawable(R.drawable.subscription_badge_trial)
//
//                        binding.tvTrialDaysLeft.text = calculateDetailedTimeLeft(trialStartDate)
//                        binding.tvTrialDaysLeft.setTextColor(
//                            when {
//                                daysLeft > 2 -> Color.parseColor("#FF5722")
//                                daysLeft > 0 -> Color.parseColor("#F44336")
//                                else -> Color.parseColor("#D32F2F")
//                            }
//                        )
//
//                        // Tampilkan upgrade card
//                        binding.cardSubscription.visibility = View.VISIBLE
//                    } else {
//                        // Trial habis → ubah status jadi expired
//                        val currentUser = auth.currentUser
//                        if (currentUser != null) {
//                            firestore.collection("users").document(currentUser.uid)
//                                .update("subscriptionStatus", "expired")
//                        }
//
//                        // Update UI expired
//                        binding.tvSubscriptionStatus.text = "Berakhir"
//                        binding.tvSubscriptionStatus.background =
//                            getDrawable(R.drawable.subscription_badge_expired)
//                        binding.tvTrialDaysLeft.text = "Trial berakhir"
//                        binding.tvTrialDaysLeft.setTextColor(Color.parseColor("#F44336"))
//                        binding.cardSubscription.visibility = View.VISIBLE
//                    }
//                }
//            }
//
//            "active" -> {
//                binding.tvSubscriptionStatus.text = "Premium"
//                binding.tvSubscriptionStatus.background =
//                    getDrawable(R.drawable.subscription_badge_premium)
//                binding.tvTrialDaysLeft.text = "Aktif"
//                binding.tvTrialDaysLeft.setTextColor(Color.parseColor("#4CAF50"))
//
//                // Sembunyikan upgrade card
//                binding.cardSubscription.visibility = View.GONE
//            }
//
//            "expired" -> {
//                binding.tvSubscriptionStatus.text = "Berakhir"
//                binding.tvSubscriptionStatus.background =
//                    getDrawable(R.drawable.subscription_badge_expired)
//                binding.tvTrialDaysLeft.text = "Perlu diperpanjang"
//                binding.tvTrialDaysLeft.setTextColor(Color.parseColor("#F44336"))
//
//                // Tampilkan upgrade card
//                binding.cardSubscription.visibility = View.VISIBLE
//            }
//        }
//    }
//
//
//    private fun calculateTrialDaysLeft(trialStartDate: com.google.firebase.Timestamp): Int {
//        val currentTime = System.currentTimeMillis()
//        val trialStart = trialStartDate.toDate().time
//        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
//        val timeLeft = (trialStart + sevenDaysInMillis) - currentTime
//
//        return if (timeLeft > 0) (timeLeft / (24 * 60 * 60 * 1000L)).toInt() else 0
//    }
//
//    private fun loadUserStatistics(userId: String) {
//        // Create scan_history collection if not exists and add sample data
//        createSampleScanHistory(userId)
//
//        // Load real statistics from Firestore
//        firestore.collection("scan_history")
//            .whereEqualTo("userId", userId)
//            .addSnapshotListener { documents, error ->
//                if (error != null) {
//                    // Set default values if query fails
//                    binding.tvTotalScans.text = "0"
//                    binding.tvHealthyCattle.text = "0"
//                    binding.tvDiseasedCattle.text = "0"
//                    return@addSnapshotListener
//                }
//
//                if (documents != null) {
//                    val totalScans = documents.size()
//                    var healthyCattle = 0
//                    var diseasedCattle = 0
//
//                    for (document in documents) {
//                        val diseaseDetected = document.getString("diseaseDetected") ?: ""
//                        if (diseaseDetected == "Tidak ada penyakit" ||
//                            diseaseDetected.contains("sehat", ignoreCase = true) ||
//                            diseaseDetected.isEmpty()) {
//                            healthyCattle++
//                        } else {
//                            diseasedCattle++
//                        }
//                    }
//
//                    // Update UI
//                    binding.tvTotalScans.text = totalScans.toString()
//                    binding.tvHealthyCattle.text = healthyCattle.toString()
//                    binding.tvDiseasedCattle.text = diseasedCattle.toString()
//                } else {
//                    // Set default values
//                    binding.tvTotalScans.text = "0"
//                    binding.tvHealthyCattle.text = "0"
//                    binding.tvDiseasedCattle.text = "0"
//                }
//            }
//    }
//
//    private fun createSampleScanHistory(userId: String) {
//        // Check if user has any scan history, if not create sample data
//        firestore.collection("scan_history")
//            .whereEqualTo("userId", userId)
//            .limit(1)
//            .get()
//            .addOnSuccessListener { documents ->
//                if (documents.isEmpty) {
//                    // Create sample scan history
//                    val sampleScans = listOf(
//                        hashMapOf(
//                            "userId" to userId,
//                            "cattleType" to "Sapi Madura",
//                            "diseaseDetected" to "Lumpy Skin Disease",
//                            "severity" to "Berat",
//                            "scanDate" to com.google.firebase.Timestamp.now(),
//                            "confidence" to 87.5
//                        ),
//                        hashMapOf(
//                            "userId" to userId,
//                            "cattleType" to "Sapi Brahman",
//                            "diseaseDetected" to "Tidak ada penyakit",
//                            "severity" to "Sehat",
//                            "scanDate" to com.google.firebase.Timestamp.now(),
//                            "confidence" to 95.2
//                        )
//                    )
//
//                    sampleScans.forEach { scanData ->
//                        firestore.collection("scan_history")
//                            .add(scanData)
//                    }
//                }
//            }
//    }
//
//    private fun setupClickListeners() {
//        // Edit Profile Button
//        binding.btnEditProfile.setOnClickListener {
//            startActivity(Intent(this, EditProfileActivity::class.java))
//        }
//
//        // Upgrade Premium Button
//        binding.btnUpgradePremium.setOnClickListener {
//            showUpgradeDialog()
//        }
//
//        // Menu Items
//        binding.menuEditProfile.setOnClickListener {
//            startActivity(Intent(this, EditProfileActivity::class.java))
//        }
//
//        binding.menuSubscription.setOnClickListener {
//            showSubscriptionManagement()
//        }
//
//        binding.menuSettings.setOnClickListener {
//            startActivity(Intent(this, SettingsActivity::class.java))
//        }
//
//        binding.menuHelp.setOnClickListener {
//            startActivity(Intent(this, HelpSupportActivity::class.java))
//        }
//
//        binding.menuLogout.setOnClickListener {
//            showLogoutConfirmation()
//        }
//    }
//
//    // ... rest of existing methods remain the same ...
//
//    private fun showUpgradeDialog() {
//        val dialog = UpgradeDialogFragment()
//        dialog.show(supportFragmentManager, "UpgradeDialog")
//    }
//
//    private fun showSubscriptionManagement() {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            firestore.collection("users").document(currentUser.uid)
//                .get()
//                .addOnSuccessListener { document ->
//                    if (document.exists()) {
//                        val subscriptionStatus = document.getString("subscriptionStatus") ?: "trial"
//                        val subscriptionType = document.getString("subscriptionType") ?: ""
//                        val subscriptionEndDate = document.getTimestamp("subscriptionEndDate")
//
//                        showSubscriptionDetails(subscriptionStatus, subscriptionType, subscriptionEndDate)
//                    }
//                }
//        }
//    }
//
//    private fun showSubscriptionDetails(status: String, type: String, endDate: com.google.firebase.Timestamp?) {
//        val message = when (status) {
//            "trial" -> "Anda sedang dalam masa trial 7 hari gratis."
//            "active" -> {
//                val endDateStr = if (endDate != null) {
//                    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(endDate.toDate())
//                } else "Tidak diketahui"
//                "Langganan $type aktif hingga $endDateStr"
//            }
//            "expired" -> "Langganan Anda telah berakhir. Upgrade untuk melanjutkan akses premium."
//            else -> "Status langganan tidak diketahui."
//        }
//
//        AlertDialog.Builder(this)
//            .setTitle("Status Langganan")
//            .setMessage(message)
//            .setPositiveButton("Upgrade") { _, _ ->
//                showUpgradeDialog()
//            }
//            .setNegativeButton("Tutup", null)
//            .show()
//    }
//
//    private fun showLogoutConfirmation() {
//        AlertDialog.Builder(this)
//            .setTitle("Konfirmasi Keluar")
//            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
//            .setPositiveButton("Keluar") { _, _ ->
//                performLogout()
//            }
//            .setNegativeButton("Batal", null)
//            .show()
//    }
//
//    private fun performLogout() {
//        auth.signOut()
//
//        // Clear any cached data if needed
//        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
//        sharedPrefs.edit().clear().apply()
//
//        // Navigate to login screen
//        val intent = Intent(this, LoginActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//        finish()
//
//        Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun setupBottomNavigation() {
//        setActiveNav(binding.navProfil)
//
//        binding.navBeranda.setOnClickListener {
//            finish() // Kembali ke MainActivity
//        }
//
//        binding.navInformasi.setOnClickListener {
//            startActivity(Intent(this, InformationActivity::class.java))
//        }
//
//        binding.fabDeteksi.setOnClickListener {
//            startActivity(Intent(this, CameraScanActivity::class.java))
//        }
//
////        binding.navRiwayat.setOnClickListener {
////            startActivity(Intent(this, HistoryActivity::class.java))
////        }
//
//        binding.navProfil.setOnClickListener {
//            setActiveNav(binding.navProfil)
//        }
//    }
//
//    private fun setActiveNav(activeNav: LinearLayout) {
//        val allNavs = listOf(binding.navBeranda, binding.navInformasi, binding.navRiwayat, binding.navProfil)
//        val activeColor = Color.parseColor("#4A90E2")
//        val inactiveColor = Color.parseColor("#B0B0B0")
//
//        for (nav in allNavs) {
//            val icon = nav.getChildAt(0) as ImageView
//            val label = nav.getChildAt(1) as TextView
//
//            if (nav == activeNav) {
//                icon.setColorFilter(activeColor)
//                label.setTextColor(activeColor)
//            } else {
//                icon.setColorFilter(inactiveColor)
//                label.setTextColor(inactiveColor)
//            }
//        }
//    }
//}
