package com.example.sadamoo.admin.adapters

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.sadamoo.R
import com.example.sadamoo.admin.models.UserModel
import com.example.sadamoo.databinding.ItemUserManagementBinding
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64

class UserManagementAdapter(
    private val users: List<UserModel>,
    private val onUserClick: (UserModel) -> Unit,
    private val onEditClick: (UserModel) -> Unit,
    private val onSubscriptionChange: (UserModel) -> Unit,
    private val onBanUser: (UserModel) -> Unit,
    private val onDeleteClick: (UserModel) -> Unit
) : RecyclerView.Adapter<UserManagementAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserManagementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserModel) {
            binding.apply {
                // User basic info
                tvUserName.text = user.name
                tvUserEmail.text = user.email
                tvUserPhone.text = if (user.phone.isNotEmpty()) user.phone else "No phone"
                tvTotalScans.text = "📊 ${user.totalScans} scans"

                // Format join date
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                tvJoinDate.text = "📅 ${dateFormat.format(user.createdAt)}"

                // Set role badge
                setupRoleBadge(user)

                // Set subscription status
                setupSubscriptionStatus(user)

                // Set ban status
                setupBanStatus(user)

                if (!user.photoBase64.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(user.photoBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(
                            imageBytes, 0, imageBytes.size
                        )

                        Glide.with(root.context)
                            .load(bitmap)
                            .transform(CircleCrop())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(ivUserAvatar)

                    } catch (e: Exception) {
                        ivUserAvatar.setImageResource(R.drawable.ic_profile)
                    }
                } else {
                    ivUserAvatar.setImageResource(R.drawable.ic_profile)
                }

                // Click listeners
                root.setOnClickListener { onUserClick(user) }

                btnEditUser.setOnClickListener { onEditClick(user) }

                btnChangeSubscription.setOnClickListener { onSubscriptionChange(user) }

                btnBanUser.text = if (user.isBanned) "🔓 Unban" else "🚫 Ban"
                btnBanUser.setOnClickListener { onBanUser(user) }

                btnDeleteUser.setOnClickListener { onDeleteClick(user) }
            }
        }

        private fun setupRoleBadge(user: UserModel) {
            binding.tvRoleBadge.apply {
                text = user.getRoleBadgeText()
                try {
                    setBackgroundColor(Color.parseColor(user.getRoleBadgeColor()))
                } catch (e: Exception) {
                    setBackgroundColor(Color.parseColor("#4A90E2"))
                }
            }
        }

        private fun setupSubscriptionStatus(user: UserModel) {
            binding.apply {
                when (user.subscriptionStatus) {
                    "trial" -> {
                        tvSubscriptionStatus.text = "Trial"
                        tvSubscriptionStatus.background =
                            itemView.context.getDrawable(R.drawable.subscription_badge_trial)

                        // Calculate trial days left
                        if (user.trialStartDate != null) {
                            val daysLeft = calculateTrialDaysLeft(user.trialStartDate)
                            tvSubscriptionDetails.text = if (daysLeft > 0) {
                                "$daysLeft hari tersisa"
                            } else {
                                "Trial berakhir"
                            }
                        } else {
                            tvSubscriptionDetails.text = "Trial period"
                        }
                    }
                    "active" -> {
                        tvSubscriptionStatus.text = "Premium"
                        tvSubscriptionStatus.background =
                            itemView.context.getDrawable(R.drawable.subscription_badge_premium)

                        val typeText = when (user.subscriptionType) {
                            "monthly" -> "Monthly"
                            "yearly" -> "Yearly"
                            else -> "Premium"
                        }

                        if (user.subscriptionEndDate != null) {
                            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                            tvSubscriptionDetails.text = "$typeText until ${dateFormat.format(user.subscriptionEndDate)}"
                        } else {
                            tvSubscriptionDetails.text = "$typeText active"
                        }
                    }
                    "expired" -> {
                        tvSubscriptionStatus.text = "Expired"
                        tvSubscriptionStatus.background =
                            itemView.context.getDrawable(R.drawable.subscription_badge_expired)
                        tvSubscriptionDetails.text = "Subscription ended"
                    }
                    else -> {
                        tvSubscriptionStatus.text = "Unknown"
                        tvSubscriptionStatus.background =
                            itemView.context.getDrawable(R.drawable.subscription_badge_expired)
                        tvSubscriptionDetails.text = "Status unknown"
                    }
                }
            }
        }

        private fun setupBanStatus(user: UserModel) {
            binding.apply {
                if (user.isBanned) {
                    tvUserName.setTextColor(Color.parseColor("#F44336"))
                    tvBanStatus.visibility = View.VISIBLE

                    // Disable some buttons for banned users
                    btnChangeSubscription.isEnabled = false
                    btnChangeSubscription.alpha = 0.5f
                } else {
                    tvUserName.setTextColor(Color.parseColor("#212121"))
                    tvBanStatus.visibility = View.GONE

                    btnChangeSubscription.isEnabled = true
                    btnChangeSubscription.alpha = 1f
                }
            }
        }

        private fun calculateTrialDaysLeft(trialStartDate: Date): Int {
            val currentTime = System.currentTimeMillis()
            val trialStart = trialStartDate.time
            val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
            val timeLeft = (trialStart + sevenDaysInMillis) - currentTime

            return if (timeLeft > 0) (timeLeft / (24 * 60 * 60 * 1000L)).toInt() else 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManagementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
}