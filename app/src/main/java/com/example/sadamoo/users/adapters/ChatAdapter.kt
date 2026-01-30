package com.example.sadamoo.users.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.databinding.ItemChatReceivedBinding
import com.example.sadamoo.databinding.ItemChatSentBinding
import com.example.sadamoo.users.models.ChatMessage
import com.example.sadamoo.users.models.MessageType
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    inner class SentMessageViewHolder(private val binding: ItemChatSentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.message
            binding.tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp)
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemChatReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.message
            binding.tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp)
//            binding.tvSenderName.text = message.senderName
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            MessageType.SENT -> TYPE_SENT
            MessageType.RECEIVED -> TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SENT -> {
                val binding = ItemChatSentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SentMessageViewHolder(binding)
            }
            TYPE_RECEIVED -> {
                val binding = ItemChatReceivedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SentMessageViewHolder -> holder.bind(messages[position])
            is ReceivedMessageViewHolder -> holder.bind(messages[position])
        }
    }

    override fun getItemCount(): Int = messages.size
}
