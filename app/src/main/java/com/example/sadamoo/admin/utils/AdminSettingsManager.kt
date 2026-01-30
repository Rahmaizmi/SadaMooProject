package com.example.sadamoo.admin.utils

import android.content.Context
import android.content.SharedPreferences

class AdminSettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("admin_settings", Context.MODE_PRIVATE)

    companion object {
        const val DARK_MODE = "dark_mode"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val AUTO_BACKUP = "auto_backup"
        const val SECURITY_MODE = "security_mode"
        const val MAINTENANCE_MODE = "maintenance_mode"
        const val LAST_BACKUP = "last_backup"
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(DARK_MODE, enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isAutoBackupEnabled(): Boolean {
        return sharedPreferences.getBoolean(AUTO_BACKUP, true)
    }

    fun setAutoBackup(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(AUTO_BACKUP, enabled).apply()
    }

    fun isSecurityModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(SECURITY_MODE, false)
    }

    fun setSecurityMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(SECURITY_MODE, enabled).apply()
    }

    fun isMaintenanceModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(MAINTENANCE_MODE, false)
    }

    fun setMaintenanceMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(MAINTENANCE_MODE, enabled).apply()
    }

    fun getLastBackupTime(): String {
        return sharedPreferences.getString(LAST_BACKUP, "Never") ?: "Never"
    }

    fun setLastBackupTime(time: String) {
        sharedPreferences.edit().putString(LAST_BACKUP, time).apply()
    }

    fun resetAllSettings() {
        sharedPreferences.edit().clear().apply()
    }
}
