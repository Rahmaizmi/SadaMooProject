package com.example.sadamoo.doctor.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validation
        if (currentPassword.isEmpty()) {
            binding.etCurrentPassword.error = "Password saat ini tidak boleh kosong"
            binding.etCurrentPassword.requestFocus()
            return
        }

        if (newPassword.isEmpty()) {
            binding.etNewPassword.error = "Password baru tidak boleh kosong"
            binding.etNewPassword.requestFocus()
            return
        }

        if (newPassword.length < 6) {
            binding.etNewPassword.error = "Password minimal 6 karakter"
            binding.etNewPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
            binding.etConfirmPassword.requestFocus()
            return
        }

        if (newPassword != confirmPassword) {
            binding.etConfirmPassword.error = "Password tidak cocok"
            binding.etConfirmPassword.requestFocus()
            return
        }

        if (currentPassword == newPassword) {
            binding.etNewPassword.error = "Password baru harus berbeda dengan password lama"
            binding.etNewPassword.requestFocus()
            return
        }

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnChangePassword.isEnabled = false

        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {
            // Re-authenticate user
            val credential = EmailAuthProvider.getCredential(email, currentPassword)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // Update password
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            binding.progressBar.visibility = android.view.View.GONE
                            Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            binding.progressBar.visibility = android.view.View.GONE
                            binding.btnChangePassword.isEnabled = true
                            Toast.makeText(this, "Gagal mengubah password: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnChangePassword.isEnabled = true
                    binding.etCurrentPassword.error = "Password saat ini salah"
                    binding.etCurrentPassword.requestFocus()
                }
        }
    }
}