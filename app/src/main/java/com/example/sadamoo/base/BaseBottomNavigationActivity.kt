package com.example.sadamoo.base

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.databinding.ActivityMainBinding
import com.example.sadamoo.users.MainActivity
import com.example.sadamoo.users.InformationActivity
//import com.example.sadamoo.users.HistoryActivity
//import com.example.sadamoo.users.ProfileActivity
import com.example.sadamoo.users.CameraScanActivity
import kotlin.jvm.java

abstract class BaseBottomNavigationActivity : AppCompatActivity() {

    // Binding akan di-override di setiap child class
    protected lateinit var bottomNavBinding: ActivityMainBinding

    protected fun setupBottomNavigation(currentActivity: String) {
        // Set active navigation berdasarkan activity saat ini
        when (currentActivity) {
            "MainActivity" -> setActiveNav(bottomNavBinding.navBeranda)
            "InformationActivity" -> setActiveNav(bottomNavBinding.navInformasi)
            "HistoryActivity" -> setActiveNav(bottomNavBinding.navRiwayat)
            "ProfileActivity" -> setActiveNav(bottomNavBinding.navProfil)
        }

        bottomNavBinding.navBeranda.setOnClickListener {
            navigateToActivity(MainActivity::class.java, "MainActivity")
        }

        bottomNavBinding.navInformasi.setOnClickListener {
            navigateToActivity(InformationActivity::class.java, "InformationActivity")
        }

//        bottomNavBinding.navRiwayat.setOnClickListener {
//            navigateToActivity(HistoryActivity::class.java, "HistoryActivity")
//        }

//        bottomNavBinding.navProfil.setOnClickListener {
//            navigateToActivity(ProfileActivity::class.java, "ProfileActivity")
//        }

        bottomNavBinding.fabDeteksi.setOnClickListener {
            startActivity(Intent(this, CameraScanActivity::class.java))
        }
    }

    private fun navigateToActivity(targetActivity: Class<*>, activityName: String) {
        // Cek apakah sudah berada di activity yang sama
        if (this::class.java.simpleName != activityName) {
            val intent = Intent(this, targetActivity)
            // Flag untuk menghindari stack activity yang berlebihan
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            // Animasi transisi yang smooth
            overridePendingTransition(0, 0)
        }
    }

    protected fun setActiveNav(activeNav: LinearLayout) {
        val allNavs = listOf(
            bottomNavBinding.navBeranda,
            bottomNavBinding.navInformasi,
            bottomNavBinding.navRiwayat,
            bottomNavBinding.navProfil
        )
        val activeColor = Color.parseColor("#4A90E2")
        val inactiveColor = Color.parseColor("#B0B0B0")

        for (nav in allNavs) {
            val icon = nav.getChildAt(0) as ImageView
            val label = nav.getChildAt(1) as TextView

            if (nav == activeNav) {
                icon.setColorFilter(activeColor)
                label.setTextColor(activeColor)
            } else {
                icon.setColorFilter(inactiveColor)
                label.setTextColor(inactiveColor)
            }
        }
    }
}
