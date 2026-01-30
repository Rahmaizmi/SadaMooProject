package com.example.sadamoo

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnDaftar.setOnClickListener {
            val nama = binding.etNamaLengkap.text.toString().trim()
            val emailHp = binding.etEmailHp.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val konfirmasi = binding.etKonfirmasiPassword.text.toString().trim()

            // Validasi input
            if (nama.isEmpty() || emailHp.isEmpty() || password.isEmpty() || konfirmasi.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (emailHp.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(emailHp).matches()) {
                Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != konfirmasi) {
                Toast.makeText(this, "Password dan konfirmasi tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register user dengan Firebase Auth
            registerUser(nama, emailHp, password)
        }

        binding.tvMasuk.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(nama: String, email: String, password: String) {
        // Show loading
        binding.btnDaftar.isEnabled = false
        binding.btnDaftar.text = "Mendaftar..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnDaftar.isEnabled = true
                binding.btnDaftar.text = "Daftar"

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Simpan data user ke Firestore
                        saveUserToFirestore(it.uid, nama, email)
                    }
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("network error") == true ->
                            "Periksa koneksi internet Anda"
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "Email sudah terdaftar"
                        task.exception?.message?.contains("weak password") == true ->
                            "Password terlalu lemah"
                        else -> "Registrasi gagal: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, nama: String, email: String) {
        val userData = hashMapOf(
            "name" to nama,
            "email" to email,
            "role" to "user",
            "subscriptionStatus" to "trial", // Set trial untuk user baru
            "trialStartDate" to com.google.firebase.Timestamp.now(), // Start trial
            "scanCount" to 0,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "isActive" to true
        )

        firestore.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Pendaftaran berhasil! Trial 7 hari dimulai.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error menyimpan data: ${exception.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }
}
