package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.databinding.DialogChangePasswordBinding

class ChangePasswordDialog : DialogFragment() {
    private lateinit var binding: DialogChangePasswordBinding
    private lateinit var onPasswordChanged: (String, String) -> Unit

    companion object {
        fun newInstance(onPasswordChanged: (String, String) -> Unit): ChangePasswordDialog {
            val dialog = ChangePasswordDialog()
            dialog.onPasswordChanged = onPasswordChanged
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validation
        if (oldPassword.isEmpty()) {
            binding.etOldPassword.error = "Current password is required"
            return
        }

        if (newPassword.isEmpty()) {
            binding.etNewPassword.error = "New password is required"
            return
        }

        if (newPassword.length < 6) {
            binding.etNewPassword.error = "Password must be at least 6 characters"
            return
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Please confirm your new password"
            return
        }

        if (newPassword != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return
        }

        if (oldPassword == newPassword) {
            binding.etNewPassword.error = "New password must be different from current password"
            return
        }

        onPasswordChanged(oldPassword, newPassword)
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
