package com.example.sadamoo.users.models

import java.util.Date

data class ChatMessage(
    val id: String,
    val message: String,
    val timestamp: Date,
    val type: MessageType,
    val senderName: String,
    val attachmentUrl: String? = null,
    val attachmentType: String? = null // "image", "pdf", etc.
)

enum class MessageType {
    SENT, RECEIVED
}
