package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.admin.models.UserModel
import com.example.sadamoo.databinding.DialogUserDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class UserDetailDialog : DialogFragment() {
    private lateinit var binding: DialogUserDetailBinding
    private lateinit var user: UserModel

    companion object {
        fun newInstance(user: UserModel): UserDetailDialog {
            val dialog = UserDetailDialog()
            val args = Bundle()
            args.putSerializable("user", user)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments?.getSerializable("user") as? UserModel ?: return
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserDetails()

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupUserDetails() {
        binding.apply {
            tvUserName.text = user.name
            tvUserEmail.text = user.email
            tvUserPhone.text = if (user.phone.isNotEmpty()) user.phone else "No phone number"
            tvTotalScans.text = "${user.totalScans} total scans"

            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            tvJoinDate.text = "Joined: ${dateFormat.format(user.createdAt)}"

            if (user.lastActive != null) {
                tvLastActive.text = "Last active: ${dateFormat.format(user.lastActive)}"
            } else {
                tvLastActive.text = "Last active: Never"
            }

            // Subscription details
            tvSubscriptionStatus.text = user.subscriptionStatus.uppercase()
            when (user.subscriptionStatus) {
                "trial" -> {
                    tvSubscriptionDetails.text = if (user.trialStartDate != null) {
                        "Trial started: ${SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(user.trialStartDate)}"
                    } else {
                        "Trial period"
                    }
                }
                "active" -> {
                    tvSubscriptionDetails.text = if (user.subscriptionEndDate != null) {
                        "Premium until: ${SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(user.subscriptionEndDate)}"
                    } else {
                        "Active premium subscription"
                    }
                }
                "expired" -> {
                    tvSubscriptionDetails.text = "Subscription has expired"
                }
            }

            // Ban status
            if (user.isBanned) {
                tvBanStatus.text = "⚠️ This user is currently BANNED"
                tvBanStatus.visibility = View.VISIBLE
            } else {
                tvBanStatus.visibility = View.GONE
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
