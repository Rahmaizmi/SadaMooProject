package com.example.sadamoo.users

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.sadamoo.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE)

        loadCurrentSettings()
        setupClickListeners()
    }

    private fun loadCurrentSettings() {
        // Load current settings
        binding.switchNotifications.isChecked = sharedPrefs.getBoolean("notifications_enabled", true)
        binding.switchDarkMode.isChecked = sharedPrefs.getBoolean("dark_mode", false)
        binding.switchAutoScan.isChecked = sharedPrefs.getBoolean("auto_scan", false)
        binding.switchDataSaver.isChecked = sharedPrefs.getBoolean("data_saver", false)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            Toast.makeText(this, if (isChecked) "Notifikasi diaktifkan" else "Notifikasi dinonaktifkan", Toast.LENGTH_SHORT).show()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.switchAutoScan.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("auto_scan", isChecked).apply()
            Toast.makeText(this, if (isChecked) "Auto scan diaktifkan" else "Auto scan dinonaktifkan", Toast.LENGTH_SHORT).show()
        }

        binding.switchDataSaver.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("data_saver", isChecked).apply()
            Toast.makeText(this, if (isChecked) "Mode hemat data diaktifkan" else "Mode hemat data dinonaktifkan", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearCache.setOnClickListener {
            clearAppCache()
        }

        binding.btnResetSettings.setOnClickListener {
            resetAllSettings()
        }
    }

    private fun clearAppCache() {
        try {
            val cacheDir = cacheDir
            cacheDir.deleteRecursively()
            Toast.makeText(this, "Cache berhasil dibersihkan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membersihkan cache", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetAllSettings() {
        sharedPrefs.edit().clear().apply()
        loadCurrentSettings()
        Toast.makeText(this, "Pengaturan direset ke default", Toast.LENGTH_SHORT).show()
    }
}
