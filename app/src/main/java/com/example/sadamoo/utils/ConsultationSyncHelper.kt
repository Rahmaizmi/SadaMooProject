package com.example.sadamoo.utils

import android.content.Context
import com.example.sadamoo.users.data.Consultation
import com.example.sadamoo.users.data.DetectionRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class ConsultationSyncHelper(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val consultationDao = DetectionRoomDatabase.getDatabase(context).consultationDao()

    suspend fun syncConsultationsFromFirestore() {
        try {
            val currentUser = auth.currentUser ?: return

            // 🔥 HAPUS DULU DATA LOKAL LAMA
            consultationDao.deleteAll()

            // Ambil data terbaru dari Firestore
            val chatRoomsSnapshot = firestore.collection("chatRooms")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val consultations = chatRoomsSnapshot.documents.mapNotNull { doc ->
                try {
                    Consultation(
                        id = doc.id,
                        doctorId = doc.getString("doctorId") ?: "",
                        doctorName = doc.getString("doctorName") ?: "Dokter",
                        lastMessage = doc.getString("lastMessage") ?: "",
                        lastSenderId = doc.getString("lastSenderId") ?: "",
                        timestamp = doc.getTimestamp("lastTimestamp")?.toDate() ?: Date(),
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            // Simpan fresh data
            consultationDao.insertAll(consultations)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}