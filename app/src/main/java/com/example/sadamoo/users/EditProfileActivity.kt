package com.example.sadamoo.users

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.sadamoo.R
import com.example.sadamoo.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    private var currentPhotoBase64: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                Log.d("EditProfile", "Image selected: $selectedImageUri")
                // Load image dengan Glide untuk preview
                Glide.with(this)
                    .load(selectedImageUri)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivProfileAvatar)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadCurrentUserData()
        setupClickListeners()
    }

    private fun loadCurrentUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.etEmail.setText(currentUser.email)

            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.etName.setText(document.getString("name") ?: "")
                        binding.etPhone.setText(document.getString("phone") ?: "")
                        binding.etAddress.setText(document.getString("address") ?: "")
                        binding.etFarmName.setText(document.getString("farmName") ?: "")
                        binding.etCattleCount.setText(document.getLong("cattleCount")?.toString() ?: "")

                        // Load existing profile photo from Base64
                        currentPhotoBase64 = document.getString("photoBase64")
                        if (!currentPhotoBase64.isNullOrEmpty()) {
                            loadBase64Image(currentPhotoBase64!!)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfile", "Error loading user data", e)
                    Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadBase64Image(base64String: String) {
        try {
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            Glide.with(this)
                .load(bitmap)
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_profile)
                .into(binding.ivProfileAvatar)
        } catch (e: Exception) {
            Log.e("EditProfile", "Error decoding base64 image", e)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.ivProfileAvatar.setOnClickListener {
            openImagePicker()
        }

        binding.btnChangePhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val farmName = binding.etFarmName.text.toString().trim()
        val cattleCountStr = binding.etCattleCount.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Nama tidak boleh kosong"
            return
        }

        val cattleCount = cattleCountStr.toLongOrNull() ?: 0

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Menyimpan..."

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Check if user selected new image
            if (selectedImageUri != null) {
                // Convert image to Base64
                val base64Image = convertImageToBase64(selectedImageUri!!)
                if (base64Image != null) {
                    saveProfileData(currentUser.uid, name, phone, address, farmName, cattleCount, base64Image)
                } else {
                    Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Simpan"
                }
            } else {
                // No new image, just save profile data
                saveProfileData(currentUser.uid, name, phone, address, farmName, cattleCount, currentPhotoBase64)
            }
        }
    }

    private fun convertImageToBase64(imageUri: Uri): String? {
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Resize image to reduce size (max 800x800)
            val resizedBitmap = resizeBitmap(bitmap, 800, 800)

            // Convert to Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("EditProfile", "Error converting image to Base64", e)
            return null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    private fun saveProfileData(
        userId: String,
        name: String,
        phone: String,
        address: String,
        farmName: String,
        cattleCount: Long,
        photoBase64: String?
    ) {
        val userData = hashMapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "farmName" to farmName,
            "cattleCount" to cattleCount,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        // Add photoBase64 only if it's not null
        if (photoBase64 != null) {
            userData["photoBase64"] = photoBase64
        }

        Log.d("EditProfile", "Saving profile data")

        firestore.collection("users").document(userId)
            .update(userData as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("EditProfile", "Profile updated successfully")
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("EditProfile", "Failed to update profile", e)
                Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Simpan"
            }
    }
}