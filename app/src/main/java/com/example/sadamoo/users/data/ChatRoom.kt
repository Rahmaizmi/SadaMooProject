package com.example.sadamoo.users.data

import com.google.firebase.Timestamp

data class ChatRoom(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val lastTimestamp: Timestamp? = null,
    var userPhotoBase64: String? = null,
)
