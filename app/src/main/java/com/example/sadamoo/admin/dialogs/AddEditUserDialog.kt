package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.sadamoo.R
import com.example.sadamoo.admin.models.UserModel
import com.example.sadamoo.databinding.DialogAddEditUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class AddEditUserDialog : DialogFragment() {

    private var _binding: DialogAddEditUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var existingUser: UserModel? = null
    private var onUserSaved: (() -> Unit)? = null

    companion object {
        private const val ARG_USER = "user"

        fun newInstance(user: UserModel? = null, onSaved: () -> Unit): AddEditUserDialog {
            return AddEditUserDialog().apply {
                arguments = Bundle().apply {
                    user?.let { putSerializable(ARG_USER, it) }
                }
                this.onUserSaved = onSaved
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        existingUser = arguments?.getSerializable(ARG_USER) as? UserModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRoleSpinner()
        setupSubscriptionSpinner()

        existingUser?.let { populateFields(it) }

        binding.btnSave.setOnClickListener { saveUser() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun setupUI() {
        val isEdit = existingUser != null
        binding.tvDialogTitle.text = if (isEdit) "Edit Pengguna" else "Tambah Pengguna Baru"
        binding.btnSave.text = if (isEdit) "Update" else "Buat Akun"

        // Hide password fields for edit mode
        if (isEdit) {
            binding.tilPassword.visibility = View.GONE
            binding.tilConfirmPassword.visibility = View.GONE
        }
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("user", "admin", "doctor")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter
    }

    private fun setupSubscriptionSpinner() {
        val subscriptions = arrayOf("trial", "active", "expired")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subscriptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubscription.adapter = adapter
    }

    private fun populateFields(user: UserModel) {
        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        binding.etPhone.setText(user.phone)

        // Set role spinner
        val rolePosition = when (user.role) {
            "admin" -> 1
            "doctor" -> 2
            else -> 0
        }
        binding.spinnerRole.setSelection(rolePosition)

        // Set subscription spinner
        val subPosition = when (user.subscriptionStatus) {
            "active" -> 1
            "expired" -> 2
            else -> 0
        }
        binding.spinnerSubscription.setSelection(subPosition)

        binding.etEmail.isEnabled = false // Email tidak bisa diubah
    }

    private fun saveUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val role = binding.spinnerRole.selectedItem.toString()
        val subscriptionStatus = binding.spinnerSubscription.selectedItem.toString()

        // Validation
        if (name.isEmpty()) {
            binding.etName.error = "Nama wajib diisi"
            return
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email tidak valid"
            return
        }

        if (existingUser == null) {
            // CREATE mode - validate password
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (password.isEmpty() || password.length < 6) {
                binding.etPassword.error = "Password minimal 6 karakter"
                return
            }

            if (password != confirmPassword) {
                binding.etConfirmPassword.error = "Password tidak cocok"
                return
            }

            createNewUser(name, email, phone, password, role, subscriptionStatus)
        } else {
            // UPDATE mode
            updateExistingUser(name, phone, role, subscriptionStatus)
        }
    }

    private fun createNewUser(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        subscriptionStatus: String
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                // Create Firebase Auth account
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception("User ID not found")

                // Create Firestore document
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "role" to role,
                    "subscriptionStatus" to subscriptionStatus,
                    "subscriptionType" to if (subscriptionStatus == "active") "monthly" else "",
                    "createdAt" to Timestamp.now(),
                    "trialStartDate" to if (subscriptionStatus == "trial") Timestamp.now() else null,
                    "subscriptionEndDate" to null,
                    "isBanned" to false,
                    "lastActive" to null
                )

                firestore.collection("users").document(userId)
                    .set(userData)
                    .await()

                Toast.makeText(requireContext(), "User berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                onUserSaved?.invoke()
                dismiss()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnSave.isEnabled = true
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateExistingUser(
        name: String,
        phone: String,
        role: String,
        subscriptionStatus: String
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val userId = existingUser?.id ?: throw Exception("User ID not found")

                val updates = hashMapOf<String, Any>(
                    "name" to name,
                    "phone" to phone,
                    "role" to role,
                    "subscriptionStatus" to subscriptionStatus,
                    "updatedAt" to Timestamp.now()
                )

                firestore.collection("users").document(userId)
                    .update(updates)
                    .await()

                Toast.makeText(requireContext(), "User berhasil diupdate", Toast.LENGTH_SHORT).show()
                onUserSaved?.invoke()
                dismiss()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnSave.isEnabled = true
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}