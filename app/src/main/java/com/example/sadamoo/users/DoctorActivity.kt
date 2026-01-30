package com.example.sadamoo.users

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.databinding.ActivityDoctorBinding
import com.example.sadamoo.users.data.ChatRoom
import com.example.sadamoo.users.adapters.ChatRoomAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.jvm.java

class DoctorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatRooms = mutableListOf<ChatRoom>()
    private lateinit var adapter: ChatRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ChatRoomAdapter(chatRooms) { chatRoom ->
            // Saat klik item, buka halaman chat
            val intent = Intent(this, DoctorChatActivity::class.java).apply {
                putExtra("chat_room_id", chatRoom.id)
                putExtra("user_name", chatRoom.userName)
                putExtra("user_id", chatRoom.userId)
            }
            startActivity(intent)
        }

        binding.rvConsultations.layoutManager = LinearLayoutManager(this)
        binding.rvConsultations.adapter = adapter

        loadChatRooms()
    }

    private fun loadChatRooms() {
        val doctorId = auth.currentUser?.uid ?: return

        db.collection("chatRooms")
            .whereEqualTo("doctorId", doctorId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    chatRooms.clear()
                    for (doc in snapshots.documents) {
                        val chatRoom = doc.toObject(ChatRoom::class.java)
                        if (chatRoom != null) {
                            chatRooms.add(chatRoom.copy(id = doc.id))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

}
