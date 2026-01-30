package com.example.sadamoo.doctor.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.sadamoo.databinding.ActivityDoctorSettingsBinding

class DoctorSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorSettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("DoctorSettings", MODE_PRIVATE)

        setupToolbar()
        loadSettings()
        setupSwitches()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        // Load saved settings
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)
        binding.switchSound.isChecked = prefs.getBoolean("sound_enabled", true)
        binding.switchVibration.isChecked = prefs.getBoolean("vibration_enabled", true)
        binding.switchAutoReply.isChecked = prefs.getBoolean("auto_reply_enabled", false)
    }

    private fun setupSwitches() {
        // Notifications
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()

            // Enable/disable other notification settings
            binding.switchSound.isEnabled = isChecked
            binding.switchVibration.isEnabled = isChecked

            if (!isChecked) {
                binding.switchSound.isChecked = false
                binding.switchVibration.isChecked = false
            }
        }

        // Sound
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()
        }

        // Vibration
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
        }

        // Auto Reply
        binding.switchAutoReply.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_reply_enabled", isChecked).apply()
        }

        // Set initial state
        val notificationsEnabled = binding.switchNotifications.isChecked
        binding.switchSound.isEnabled = notificationsEnabled
        binding.switchVibration.isEnabled = notificationsEnabled
    }
}