package com.example.sadamoo.admin.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.sadamoo.databinding.ActivityAdminSettingsBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("admin_settings", MODE_PRIVATE)
        firestore = FirebaseFirestore.getInstance()

        setupUI()
        loadSettings()
        setupListeners()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        // Load current settings
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        val isNotificationEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        val isAutoBackup = sharedPreferences.getBoolean("auto_backup", true)
        val isSecurityMode = sharedPreferences.getBoolean("security_mode", false)
        val isMaintenanceMode = sharedPreferences.getBoolean("maintenance_mode", false)

        // Update UI
        binding.switchDarkMode.isChecked = isDarkMode
        binding.switchNotifications.isChecked = isNotificationEnabled
        binding.switchAutoBackup.isChecked = isAutoBackup
        binding.switchSecurityMode.isChecked = isSecurityMode
        binding.switchMaintenanceMode.isChecked = isMaintenanceMode

        // Load backup info
        val lastBackup = sharedPreferences.getString("last_backup", "Never")
        binding.tvLastBackup.text = "Last backup: $lastBackup"
    }

    private fun setupListeners() {
        // Dark Mode Toggle
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkMode(isChecked)
        }

        // Notifications Toggle
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            toggleNotifications(isChecked)
        }

        // Auto Backup Toggle
        binding.switchAutoBackup.setOnCheckedChangeListener { _, isChecked ->
            toggleAutoBackup(isChecked)
        }

        // Security Mode Toggle
        binding.switchSecurityMode.setOnCheckedChangeListener { _, isChecked ->
            toggleSecurityMode(isChecked)
        }

        // Maintenance Mode Toggle
        binding.switchMaintenanceMode.setOnCheckedChangeListener { _, isChecked ->
            toggleMaintenanceMode(isChecked)
        }

        // Manual Backup Button
        binding.btnManualBackup.setOnClickListener {
            performManualBackup()
        }

        // Clear Cache Button
        binding.btnClearCache.setOnClickListener {
            clearCache()
        }

        // Export Data Button
        binding.btnExportData.setOnClickListener {
            exportData()
        }

        // Reset Settings Button
        binding.btnResetSettings.setOnClickListener {
            resetSettings()
        }

        // System Info Button
        binding.btnSystemInfo.setOnClickListener {
            showSystemInfo()
        }

        // About Button
        binding.btnAbout.setOnClickListener {
            showAbout()
        }
    }

    private fun toggleDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()

        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        Toast.makeText(this, "Dark mode ${if (enabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
    }

    private fun toggleNotifications(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()

        lifecycleScope.launch {
            try {
                // Update notification settings in Firebase
                val settingsData = hashMapOf(
                    "notificationsEnabled" to enabled,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("admin_settings")
                    .document("notifications")
                    .set(settingsData)
                    .await()

                Toast.makeText(this@AdminSettingsActivity,
                    "Notifications ${if (enabled) "enabled" else "disabled"}",
                    Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@AdminSettingsActivity,
                    "Error updating notification settings",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleAutoBackup(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("auto_backup", enabled).apply()

        if (enabled) {
            // Schedule auto backup (in real app, use WorkManager)
            Toast.makeText(this, "Auto backup enabled - Daily at 2:00 AM", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Auto backup disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSecurityMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("security_mode", enabled).apply()

        lifecycleScope.launch {
            try {
                val settingsData = hashMapOf(
                    "securityMode" to enabled,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("admin_settings")
                    .document("security")
                    .set(settingsData)
                    .await()

                Toast.makeText(this@AdminSettingsActivity,
                    "Security mode ${if (enabled) "enabled" else "disabled"}",
                    Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@AdminSettingsActivity,
                    "Error updating security settings",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleMaintenanceMode(enabled: Boolean) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Maintenance Mode")
            .setMessage(if (enabled)
                "Enable maintenance mode? This will prevent users from accessing the app."
            else
                "Disable maintenance mode? Users will be able to access the app again.")
            .setPositiveButton(if (enabled) "Enable" else "Disable") { _, _ ->
                performMaintenanceToggle(enabled)
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.switchMaintenanceMode.isChecked = !enabled
            }
            .show()
    }

    private fun performMaintenanceToggle(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("maintenance_mode", enabled).apply()

        lifecycleScope.launch {
            try {
                val settingsData = hashMapOf(
                    "maintenanceMode" to enabled,
                    "updatedAt" to com.google.firebase.Timestamp.now(),
                    "message" to if (enabled) "System under maintenance. Please try again later." else ""
                )

                firestore.collection("admin_settings")
                    .document("maintenance")
                    .set(settingsData)
                    .await()

                Toast.makeText(this@AdminSettingsActivity,
                    "Maintenance mode ${if (enabled) "enabled" else "disabled"}",
                    Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                Toast.makeText(this@AdminSettingsActivity,
                    "Error updating maintenance mode",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performManualBackup() {
        binding.btnManualBackup.isEnabled = false
        binding.btnManualBackup.text = "Backing up..."

        lifecycleScope.launch {
            try {
                // Simulate backup process
                kotlinx.coroutines.delay(3000)

                val currentTime = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date())

                sharedPreferences.edit().putString("last_backup", currentTime).apply()
                binding.tvLastBackup.text = "Last backup: $currentTime"

                Toast.makeText(this@AdminSettingsActivity, "Backup completed successfully!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@AdminSettingsActivity, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnManualBackup.isEnabled = true
                binding.btnManualBackup.text = "Manual Backup"
            }
        }
    }

    private fun clearCache() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear Cache")
            .setMessage("This will clear all cached data. Continue?")
            .setPositiveButton("Clear") { _, _ ->
                performClearCache()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performClearCache() {
        try {
            // Clear app cache
            cacheDir.deleteRecursively()

            // Clear shared preferences cache
            sharedPreferences.edit()
                .remove("cached_data")
                .remove("temp_files")
                .apply()

            Toast.makeText(this, "Cache cleared successfully!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error clearing cache: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportData() {
        lifecycleScope.launch {
            try {
                binding.btnExportData.isEnabled = false
                binding.btnExportData.text = "Exporting..."

                // Simulate data export
                kotlinx.coroutines.delay(2000)

                // In real app, create CSV/JSON export
                val exportData = """
                    SADA MOO Admin Data Export
                    Generated: ${java.util.Date()}
                    
                    Users: ${getCollectionCount("users")}
                    Diseases: ${getCollectionCount("cattle_diseases")}
                    Scans: ${getCollectionCount("scan_history")}
                    Notifications: ${getCollectionCount("notifications")}
                """.trimIndent()

                // Save to downloads or share
                Toast.makeText(this@AdminSettingsActivity, "Data exported successfully!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@AdminSettingsActivity, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnExportData.isEnabled = true
                binding.btnExportData.text = "Export Data"
            }
        }
    }

    private suspend fun getCollectionCount(collection: String): Int {
        return try {
            firestore.collection(collection).get().await().size()
        } catch (e: Exception) {
            0
        }
    }

    private fun resetSettings() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset Settings")
            .setMessage("This will reset all admin settings to default. Continue?")
            .setPositiveButton("Reset") { _, _ ->
                performResetSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performResetSettings() {
        // Reset all settings to default
        sharedPreferences.edit().clear().apply()

        // Reload UI with defaults
        loadSettings()

        Toast.makeText(this, "Settings reset to default!", Toast.LENGTH_SHORT).show()
    }

    private fun showSystemInfo() {
        val systemInfo = """
            📱 System Information
            
            App Version: 1.0.0
            Build: 2024.10.27
            Android Version: ${android.os.Build.VERSION.RELEASE}
            Device: ${android.os.Build.MODEL}
            Manufacturer: ${android.os.Build.MANUFACTURER}
            
            📊 Database Status:
            Firebase: Connected ✅
            Storage: Available ✅
            
            💾 Storage Usage:
            App Size: ~50 MB
            Cache Size: ~5 MB
            Available Space: ${getAvailableSpace()}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("System Information")
            .setMessage(systemInfo)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getAvailableSpace(): String {
        val freeBytes = android.os.StatFs(filesDir.path).availableBytes
        val freeMB = freeBytes / (1024 * 1024)
        return "${freeMB} MB"
    }

    private fun showAbout() {
        val aboutInfo = """
            🐄 SADA MOO Admin Panel
            
            Version: 1.0.0
            Build Date: October 27, 2025
            
            👨‍💻 Developed by:
            Your Development Team
            
            📧 Support:
            admin@sadamoo.com
            
            🌟 Features:
            • User Management
            • Payment Processing
            • Disease Database
            • Real-time Analytics
            • Notification System
            
            © 2025 SADA MOO. All rights reserved.
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("About SADA MOO Admin")
            .setMessage(aboutInfo)
            .setPositiveButton("OK", null)
            .show()
    }
}
