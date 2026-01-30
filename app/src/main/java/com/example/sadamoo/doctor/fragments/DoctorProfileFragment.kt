package com.example.sadamoo.doctor.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.sadamoo.R
import com.example.sadamoo.databinding.FragmentDoctorProfileBinding
import com.example.sadamoo.doctor.activities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.sadamoo.LoginActivity


class DoctorProfileFragment : Fragment() {

    private var _binding: FragmentDoctorProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ================= IMAGE PICKER =================

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    handleImageResult(uri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDoctorProfile()
        setupClickListeners()
    }

    // ================= LOAD PROFILE =================

    private fun loadDoctorProfile() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                // ===== BASIC DATA =====
                binding.tvDoctorName.text =
                    doc.getString("name") ?: "Dokter"

                binding.tvEmail.text =
                    doc.getString("email") ?: "-"

                // ===== OPTIONAL DATA (BELUM ADA DI FIRESTORE) =====
                binding.tvPhone.text =
                    doc.getString("phone") ?: "-"

                binding.tvLicense.text =
                    doc.getString("license") ?: "-"

                binding.tvSpecialization.text =
                    doc.getString("specialization") ?: "Dokter Hewan"

                // ===== FOTO =====
                val photoBase64 = doc.getString("photoBase64")
                if (!photoBase64.isNullOrEmpty()) {
                    val bitmap = decodeBase64ToBitmap(photoBase64)

                    Glide.with(this)
                        .load(bitmap)
                        .transform(CircleCrop())
                        .placeholder(R.drawable.ic_admin_profile)
                        .into(binding.imgDoctorAvatar)
                } else {
                    binding.imgDoctorAvatar.setImageResource(R.drawable.ic_admin_profile)
                }

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
            }
    }


    // ================= CLICK =================

    private fun setupClickListeners() {

        binding.fabEditPhoto.setOnClickListener {
            showPhotoOptionDialog()
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditDoctorProfileActivity::class.java))
        }

        binding.btnChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), DoctorSettingsActivity::class.java))
        }

        binding.btnHelp.setOnClickListener {
            startActivity(Intent(requireContext(), DoctorHelpActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    // ================= FOTO BASE64 =================

    private fun showPhotoOptionDialog() {
        val options = arrayOf("Ganti Foto", "Hapus Foto")

        AlertDialog.Builder(requireContext())
            .setTitle("Foto Profil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openImagePicker()
                    1 -> confirmDeletePhoto()
                }
            }
            .show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun handleImageResult(uri: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(
            requireActivity().contentResolver, uri
        )

        val base64 = encodeBitmapToBase64(bitmap)
        savePhotoBase64(base64)
    }

    private fun savePhotoBase64(base64: String) {
        Glide.with(this)
            .load(decodeBase64ToBitmap(base64))
            .transform(CircleCrop())
            .into(binding.imgDoctorAvatar)

        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .update("photoBase64", base64)
            .addOnSuccessListener {
                binding.imgDoctorAvatar.setImageBitmap(
                    decodeBase64ToBitmap(base64)
                )
                Toast.makeText(requireContext(), "Foto profil diperbarui", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDeletePhoto() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Foto")
            .setMessage("Yakin ingin menghapus foto profil?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteProfilePhoto()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteProfilePhoto() {
        val userId = auth.currentUser?.uid ?: return

        // ⭐ JANGAN NULL → STRING KOSONG
        db.collection("users").document(userId)
            .update("photoBase64", "")
            .addOnSuccessListener {
                binding.imgDoctorAvatar.setImageResource(R.drawable.ic_admin_profile)
                Toast.makeText(requireContext(), "Foto profil dihapus", Toast.LENGTH_SHORT).show()
            }
    }

    // ================= UTIL BASE64 =================

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun decodeBase64ToBitmap(base64: String): Bitmap {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    // ================= LOGOUT =================

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Keluar")
            .setMessage("Yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
