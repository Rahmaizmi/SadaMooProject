package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.databinding.DialogEditAdminProfileBinding

class EditAdminProfileDialog : DialogFragment() {
    private lateinit var binding: DialogEditAdminProfileBinding
    private lateinit var onProfileUpdated: (String, String) -> Unit

    companion object {
        fun newInstance(onProfileUpdated: (String, String) -> Unit): EditAdminProfileDialog {
            val dialog = EditAdminProfileDialog()
            dialog.onProfileUpdated = onProfileUpdated
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogEditAdminProfileBinding.inflate(inflater, container, false)
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

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val name = binding.etAdminName.text.toString().trim()
        val phone = binding.etAdminPhone.text.toString().trim()

        if (name.isEmpty()) {
            binding.etAdminName.error = "Name is required"
            return
        }

        onProfileUpdated(name, phone)
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
