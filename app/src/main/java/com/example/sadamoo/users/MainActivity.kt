package com.example.sadamoo.users

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sadamoo.R
import com.example.sadamoo.databinding.ActivityMainBinding
import com.example.sadamoo.users.fragments.InformationFragment
import com.example.sadamoo.users.fragments.HistoryFragment
import com.example.sadamoo.users.fragments.ProfileFragment
import com.example.sadamoo.utils.applyStatusBarPadding
import com.google.firebase.auth.FirebaseAuth
import com.example.sadamoo.users.fragments.HomeFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.root.applyStatusBarPadding()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Load home fragment by default
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        setupBottomNavigation()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("MainActivity", "Tidak ada user yang login, kembali ke LoginActivity")
            startActivity(Intent(this, com.example.sadamoo.LoginActivity::class.java))
            finish()
        } else {
            Log.d("MainActivity", "User login: ${currentUser.uid}")
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setupBottomNavigation() {
        // Default: Beranda aktif
        setActiveNav(binding.navBeranda)

        binding.navBeranda.setOnClickListener {
            replaceFragment(HomeFragment())
            setActiveNav(binding.navBeranda)
        }

        binding.navInformasi.setOnClickListener {
            replaceFragment(InformationFragment())
            setActiveNav(binding.navInformasi)
        }

        binding.navRiwayat.setOnClickListener {
            replaceFragment(HistoryFragment())
            setActiveNav(binding.navRiwayat)
        }

        binding.navProfil.setOnClickListener {
            replaceFragment(ProfileFragment())
            setActiveNav(binding.navProfil)
        }

        binding.fabDeteksi.setOnClickListener {
            Log.d("MainActivity", "🔥 FAB Deteksi DIKLIK!")
            checkScanPermission()
        }
    }

    private fun checkScanPermission() {
        Log.d("MainActivity", "🔍 checkScanPermission() dipanggil")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val subscriptionStatus = document.getString("subscriptionStatus") ?: "trial"
                        val trialStartDate = document.getTimestamp("trialStartDate")

                        when (subscriptionStatus) {
                            "trial" -> {
                                if (isTrialExpired(trialStartDate)) {
                                    showUpgradeDialog()
                                } else {
                                    startCameraScan()
                                }
                            }
                            "active" -> startCameraScan()
                            "expired" -> showUpgradeDialog()
                            else -> startCameraScan()
                        }
                    } else {
                        startCameraScan()
                    }
                }
                .addOnFailureListener {
                    startCameraScan()
                }
        }
    }

    private fun isTrialExpired(trialStartDate: com.google.firebase.Timestamp?): Boolean {
        if (trialStartDate == null) return false

        val currentTime = System.currentTimeMillis()
        val trialStart = trialStartDate.toDate().time
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L

        return (currentTime - trialStart) > sevenDaysInMillis
    }

    private fun startCameraScan() {
        startActivity(Intent(this, CameraScanActivity::class.java))
    }

    private fun showUpgradeDialog() {
        val dialog = com.example.sadamoo.users.dialogs.UpgradeDialogFragment()
        dialog.show(supportFragmentManager, "UpgradeDialog")
    }

    /**
     * Fungsi untuk mengatur navigasi aktif dengan style yang konsisten
     * Menggunakan color resources yang telah didefinisikan
     */
    private fun setActiveNav(activeNav: LinearLayout) {
        val allNavs = listOf(
            binding.navBeranda,
            binding.navInformasi,
            binding.navRiwayat,
            binding.navProfil
        )

        // Menggunakan color dari resources
        val activeColor = ContextCompat.getColor(this, R.color.nav_active)
        val inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive)

        for (nav in allNavs) {
            val icon = nav.getChildAt(0) as ImageView
            val label = nav.getChildAt(1) as TextView

            if (nav == activeNav) {
                // Set active style
                icon.setColorFilter(activeColor)
                label.setTextColor(activeColor)
                label.typeface = android.graphics.Typeface.DEFAULT_BOLD
            } else {
                // Set inactive style
                icon.setColorFilter(inactiveColor)
                label.setTextColor(inactiveColor)
                label.typeface = android.graphics.Typeface.DEFAULT
            }
        }
    }
}