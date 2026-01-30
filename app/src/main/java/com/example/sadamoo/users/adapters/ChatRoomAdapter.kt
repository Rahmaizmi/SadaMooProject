package com.example.sadamoo.users.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.sadamoo.R
import com.example.sadamoo.users.data.ChatRoom
import java.text.SimpleDateFormat
import java.util.Locale

class ChatRoomAdapter(
    private val chatRooms: List<ChatRoom>,
    private val onItemClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar) // ⭐ TAMBAHAN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = chatRooms[position]

        holder.tvUserName.text = chatRoom.userName
        holder.tvLastMessage.text = chatRoom.lastMessage

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.tvTime.text = chatRoom.lastTimestamp?.toDate()?.let { sdf.format(it) } ?: ""

        // ================= LOAD FOTO PROFIL =================
        if (!chatRoom.userPhotoBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(chatRoom.userPhotoBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                Glide.with(holder.itemView.context)
                    .load(bitmap)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_admin_profile)
                    .into(holder.imgAvatar)

            } catch (e: Exception) {
                holder.imgAvatar.setImageResource(R.drawable.ic_admin_profile)
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_admin_profile)
        }
        // =====================================================

        holder.itemView.setOnClickListener {
            onItemClick(chatRoom)
        }
    }

    override fun getItemCount() = chatRooms.size
}
