package com.example.sadamoo.admin

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sadamoo.R
import com.example.sadamoo.admin.fragments.DashboardFragment
import com.example.sadamoo.admin.fragments.UserManagementFragment
import com.example.sadamoo.admin.fragments.PaymentManagementFragment
import com.example.sadamoo.admin.fragments.DiseaseManagementFragment
//import com.example.sadamoo.admin.fragments.NotificationFragment
//import com.example.sadamoo.admin.fragments.ReportsFragment
import com.example.sadamoo.admin.fragments.AdminProfileFragment
import com.example.sadamoo.databinding.ActivityAdminBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load default fragment (Dashboard)
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_users -> {
                    loadFragment(UserManagementFragment())
                    true
                }
                R.id.nav_payments -> {
                    loadFragment(PaymentManagementFragment())
                    true
                }
                R.id.nav_diseases -> {
                    loadFragment(DiseaseManagementFragment())
                    true
                }
//                R.id.nav_notifications -> {
//                    loadFragment(NotificationFragment())
//                    true
//                }
//                R.id.nav_reports -> {
//                    loadFragment(ReportsFragment())
//                    true
//                }
                R.id.nav_profile -> {
                    loadFragment(AdminProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }


}
