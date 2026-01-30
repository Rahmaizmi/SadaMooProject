package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.admin.models.UserModel
import com.example.sadamoo.databinding.DialogChangeSubscriptionBinding
import java.util.*

class ChangeSubscriptionDialog : DialogFragment() {
    private var _binding: DialogChangeSubscriptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var user: UserModel
    private var onSubscriptionChanged: ((UserModel) -> Unit)? = null

    private var selectedStatus = "trial"
    private var selectedType = ""
    private var selectedDuration = 1 // months

    companion object {
        private const val ARG_USER = "user"

        fun newInstance(
            user: UserModel,
            onSubscriptionChanged: (UserModel) -> Unit
        ): ChangeSubscriptionDialog {
            val dialog = ChangeSubscriptionDialog()
            val args = Bundle()
            args.putSerializable(ARG_USER, user) // Changed from putParcelable to putSerializable
            dialog.arguments = args
            dialog.onSubscriptionChanged = onSubscriptionChanged
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments?.getSerializable(ARG_USER) as? UserModel ?: return // Changed from getParcelable
        selectedStatus = user.subscriptionStatus
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChangeSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCurrentSubscription()
        setupSubscriptionOptions()
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupCurrentSubscription() {
        binding.apply {
            tvUserName.text = user.name
            tvCurrentStatus.text = "Current: ${user.subscriptionStatus.uppercase()}"

            when (user.subscriptionStatus) {
                "trial" -> {
                    tvCurrentStatus.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
                }
                "active" -> {
                    tvCurrentStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                }
                "expired" -> {
                    tvCurrentStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                }
            }
        }
    }

    private fun setupSubscriptionOptions() {
        // Set default selection
        setSubscriptionStatus(selectedStatus)

        binding.apply {
            // Status selection
            btnTrial.setOnClickListener { setSubscriptionStatus("trial") }
            btnActive.setOnClickListener { setSubscriptionStatus("active") }
            btnExpired.setOnClickListener { setSubscriptionStatus("expired") }

            // Premium type selection (only visible when active is selected)
            btnBasic.setOnClickListener { setPremiumType("Basic", 1) }
            btnStandard.setOnClickListener { setPremiumType("Standard", 3) }
            btnPremium.setOnClickListener { setPremiumType("Premium", 6) }
            btnUltimate.setOnClickListener { setPremiumType("Ultimate", 12) }

            // Duration buttons
            btn1Month.setOnClickListener { setDuration(1) }
            btn3Months.setOnClickListener { setDuration(3) }
            btn6Months.setOnClickListener { setDuration(6) }
            btn12Months.setOnClickListener { setDuration(12) }
        }
    }

    private fun setSubscriptionStatus(status: String) {
        selectedStatus = status

        // Reset button styles
        binding.btnTrial.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.subscription_option_inactive)
        binding.btnActive.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.subscription_option_inactive)
        binding.btnExpired.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.subscription_option_inactive)

        // Set active button
        when (status) {
            "trial" -> {
                binding.btnTrial.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.subscription_option_active)
                binding.layoutPremiumOptions.visibility = View.GONE
                binding.layoutDurationOptions.visibility = View.GONE
            }
            "active" -> {
                binding.btnActive.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.subscription_option_active)
                binding.layoutPremiumOptions.visibility = View.VISIBLE
                binding.layoutDurationOptions.visibility = View.VISIBLE
                if (selectedType.isEmpty()) {
                    setPremiumType("Basic", 1) // Set default only if not already set
                }
            }
            "expired" -> {
                binding.btnExpired.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.subscription_option_active)
                binding.layoutPremiumOptions.visibility = View.GONE
                binding.layoutDurationOptions.visibility = View.GONE
            }
        }

        updatePreview()
    }

    private fun setPremiumType(type: String, defaultDuration: Int) {
        selectedType = type
        selectedDuration = defaultDuration

        // Reset premium type buttons
        binding.btnBasic.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.premium_type_inactive)
        binding.btnStandard.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.premium_type_inactive)
        binding.btnPremium.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.premium_type_inactive)
        binding.btnUltimate.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.premium_type_inactive)

        // Set active premium type
        when (type) {
            "Basic" -> binding.btnBasic.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.premium_type_active)
            "Standard" -> binding.btnStandard.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.premium_type_active)
            "Premium" -> binding.btnPremium.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.premium_type_active)
            "Ultimate" -> binding.btnUltimate.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.premium_type_active)
        }

        // Set default duration for this type
        setDuration(defaultDuration)
    }

    private fun setDuration(months: Int) {
        selectedDuration = months

        // Reset duration buttons
        binding.btn1Month.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_option_inactive)
        binding.btn3Months.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_option_inactive)
        binding.btn6Months.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_option_inactive)
        binding.btn12Months.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_option_inactive)

        // Set active duration
        when (months) {
            1 -> binding.btn1Month.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_option_active)
            3 -> binding.btn3Months.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_option_active)
            6 -> binding.btn6Months.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_option_active)
            12 -> binding.btn12Months.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_option_active)
        }

        updatePreview()
    }

    private fun updatePreview() {
        val preview = when (selectedStatus) {
            "trial" -> "User will have 7 days trial access"
            "active" -> "User will have $selectedType subscription for $selectedDuration month(s)"
            "expired" -> "User subscription will be marked as expired"
            else -> "Unknown status"
        }

        binding.tvPreview.text = preview
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            applySubscriptionChange()
        }
    }

    private fun applySubscriptionChange() {
        // Calculate end date for active subscriptions
        val endDate = if (selectedStatus == "active") {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, selectedDuration)
            calendar.time
        } else null

        // Update user model - create new instance dengan data updated
        val updatedUser = UserModel(
            id = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone,
            role = user.role,
            subscriptionStatus = selectedStatus,
            subscriptionType = if (selectedStatus == "active") selectedType else null,
            createdAt = user.createdAt,
            lastActive = user.lastActive,
            trialStartDate = user.trialStartDate,
            subscriptionEndDate = endDate,
            isBanned = user.isBanned,
            totalScans = user.totalScans
        )

        // Call callback
        onSubscriptionChanged?.invoke(updatedUser)

        Toast.makeText(requireContext(), "Subscription updated successfully!", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}