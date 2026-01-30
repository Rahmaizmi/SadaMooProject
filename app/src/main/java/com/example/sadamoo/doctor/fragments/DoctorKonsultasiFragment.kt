package com.example.sadamoo.doctor.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.databinding.FragmentDoctorKonsultasiBinding
import com.example.sadamoo.users.DoctorChatActivity
import com.example.sadamoo.users.adapters.ChatRoomAdapter
import com.example.sadamoo.users.data.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DoctorKonsultasiFragment : Fragment() {

    private var _binding: FragmentDoctorKonsultasiBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatRooms = mutableListOf<ChatRoom>()
    private lateinit var adapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorKonsultasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatRoomAdapter(chatRooms) { chatRoom ->
            val intent = Intent(requireContext(), DoctorChatActivity::class.java).apply {
                putExtra("chat_room_id", chatRoom.id)
                putExtra("user_name", chatRoom.userName)
                putExtra("user_id", chatRoom.userId)
            }
            startActivity(intent)
        }

        binding.rvConsultations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConsultations.adapter = adapter

        loadChatRooms()
    }

    private fun loadChatRooms() {
        val doctorId = auth.currentUser?.uid ?: return

        db.collection("chatRooms")
            .whereEqualTo("doctorId", doctorId)
            .get()
            .addOnSuccessListener { snapshots ->

                chatRooms.clear()

                val sorted = snapshots.documents.sortedByDescending {
                    it.getTimestamp("lastTimestamp")?.toDate()
                }

                for (doc in sorted) {
                    val chatRoom = doc.toObject(ChatRoom::class.java) ?: continue
                    chatRooms.add(chatRoom.copy(id = doc.id))
                }

                adapter.notifyDataSetChanged()
            }

    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
