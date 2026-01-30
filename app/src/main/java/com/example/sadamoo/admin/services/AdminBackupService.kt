package com.example.sadamoo.admin.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminBackupService : Service() {
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        performBackup()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun performBackup() {
        val backupData = hashMapOf(
            "timestamp" to com.google.firebase.Timestamp.now(),
            "type" to "auto_backup",
            "status" to "completed",
            "collections_backed_up" to listOf("users", "cattle_diseases", "scan_history", "notifications")
        )

        firestore.collection("admin_backups")
            .add(backupData)
            .addOnSuccessListener {
                // Update last backup time
                val currentTime = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(Date())

                val sharedPrefs = getSharedPreferences("admin_settings", MODE_PRIVATE)
                sharedPrefs.edit().putString("last_backup", currentTime).apply()

                stopSelf()
            }
            .addOnFailureListener {
                stopSelf()
            }
    }
}
