package com.example.sadamoo.doctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sadamoo.R
import com.example.sadamoo.databinding.ActivityMainDoctorBinding
import com.example.sadamoo.doctor.fragments.DoctorKonsultasiFragment
import com.example.sadamoo.doctor.fragments.DoctorProfileFragment

class MainDoctorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainDoctorBinding
    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainDoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment (Konsultasi)
        if (savedInstanceState == null) {
            loadFragment(DoctorKonsultasiFragment())
            setKonsultasiActive()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.navKonsultasi.setOnClickListener {
            loadFragment(DoctorKonsultasiFragment())
            setKonsultasiActive()
        }

        binding.navProfil.setOnClickListener {
            loadFragment(DoctorProfileFragment())
            setProfilActive()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        if (activeFragment?.javaClass == fragment.javaClass) return

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
            commit()
        }
        activeFragment = fragment
    }

    private fun setKonsultasiActive() {
        // Konsultasi Active
        binding.iconKonsultasi.setColorFilter(
            ContextCompat.getColor(this, R.color.nav_active)
        )
        binding.labelKonsultasi.setTextColor(
            ContextCompat.getColor(this, R.color.nav_active)
        )
        binding.labelKonsultasi.setTypeface(null, android.graphics.Typeface.BOLD)

        // Profil Inactive
        binding.iconProfil.setColorFilter(
            ContextCompat.getColor(this, R.color.nav_inactive)
        )
        binding.labelProfil.setTextColor(
            ContextCompat.getColor(this, R.color.nav_inactive)
        )
        binding.labelProfil.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun setProfilActive() {
        // Konsultasi Inactive
        binding.iconKonsultasi.setColorFilter(
            ContextCompat.getColor(this, R.color.nav_inactive)
        )
        binding.labelKonsultasi.setTextColor(
            ContextCompat.getColor(this, R.color.nav_inactive)
        )
        binding.labelKonsultasi.setTypeface(null, android.graphics.Typeface.NORMAL)

        // Profil Active
        binding.iconProfil.setColorFilter(
            ContextCompat.getColor(this, R.color.nav_active)
        )
        binding.labelProfil.setTextColor(
            ContextCompat.getColor(this, R.color.nav_active)
        )
        binding.labelProfil.setTypeface(null, android.graphics.Typeface.BOLD)
    }
}