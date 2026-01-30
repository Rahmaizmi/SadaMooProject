package com.example.sadamoo.doctor.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.databinding.ActivityEditDoctorProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditDoctorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDoctorProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadCurrentData()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadCurrentData() {
        val userId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = android.view.View.VISIBLE

        db.collection("doctors").document(userId)
            .get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = android.view.View.GONE

                if (document != null && document.exists()) {
                    binding.etName.setText(document.getString("name") ?: "")
                    binding.etPhone.setText(document.getString("phone") ?: "")
                    binding.etSpecialization.setText(document.getString("specialization") ?: "")
                    binding.etLicense.setText(document.getString("license") ?: "")
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val specialization = binding.etSpecialization.text.toString().trim()
        val license = binding.etLicense.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            binding.etName.error = "Nama tidak boleh kosong"
            binding.etName.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "No. telepon tidak boleh kosong"
            binding.etPhone.requestFocus()
            return
        }

        if (specialization.isEmpty()) {
            binding.etSpecialization.error = "Spesialisasi tidak boleh kosong"
            binding.etSpecialization.requestFocus()
            return
        }

        if (license.isEmpty()) {
            binding.etLicense.error = "No. STR tidak boleh kosong"
            binding.etLicense.requestFocus()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnSave.isEnabled = false

        val updates = hashMapOf(
            "name" to name,
            "phone" to phone,
            "specialization" to specialization,
            "license" to license
        )

        db.collection("doctors").document(userId)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}