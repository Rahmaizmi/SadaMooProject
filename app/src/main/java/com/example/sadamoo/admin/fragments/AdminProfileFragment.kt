package com.example.sadamoo.admin.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sadamoo.R
import com.example.sadamoo.admin.activities.AdminSettingsActivity
import com.example.sadamoo.admin.dialogs.ChangePasswordDialog
import com.example.sadamoo.admin.dialogs.EditAdminProfileDialog
import com.example.sadamoo.databinding.FragmentAdminProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.appcompat.app.AlertDialog
import java.util.*

class AdminProfileFragment : Fragment() {
    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
//    private lateinit var storage: FirebaseStorage

    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
                // Show selected image
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivAdminPhoto)

                // Convert ke Base64 & simpan
            saveAdminPhotoBase64(uri)
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
//        storage = FirebaseStorage.getInstance()

        binding.ivAdminPhoto.setOnLongClickListener {
            showDeletePhotoDialog()
            true
        }

        setupButtons()
        loadAdminProfile()
        loadAdminStats()
    }

    private fun showDeletePhotoDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Foto Profil")
            .setMessage("Apakah kamu yakin ingin menghapus foto profil admin?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteAdminPhoto()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteAdminPhoto() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("admins")
            .document(uid)
            .update("photoBase64", "")
            .addOnSuccessListener {
                binding.ivAdminPhoto.setImageResource(R.drawable.ic_profile)
                Toast.makeText(
                    requireContext(),
                    "Foto profil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Gagal menghapus foto",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun setupButtons() {
        binding.btnEditProfile.setOnClickListener {
            editProfile()
        }

        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }

        binding.btnChangePhoto.setOnClickListener {
            changeProfilePhoto()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.btnSettings.setOnClickListener {
            openSettings()
        }

//        binding.btnDownloadUserReport.setOnClickListener {
//            downloadReport("users")
//        }
//
//        binding.btnDownloadPaymentReport.setOnClickListener {
//            downloadReport("payments")
//        }
//
//        binding.btnDownloadOtherReport.setOnClickListener {
//            downloadReport("others")
//        }

    }

private fun saveAdminPhotoBase64(uri: Uri) {
    lifecycleScope.launch {
        try {
            val currentUser = auth.currentUser ?: return@launch

            val bitmap = MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                uri
            )

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()

            val base64String = Base64.encodeToString(
                imageBytes,
                Base64.DEFAULT
            )

            firestore.collection("admins")
                .document(currentUser.uid)
                .update("photoBase64", base64String)
                .await()

            Toast.makeText(
                requireContext(),
                "Foto admin berhasil diperbarui",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Gagal menyimpan foto: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


//    private fun downloadReport(type: String) {
//        // Minta izin tulis file jika belum diberikan
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                1
//            )
//            Toast.makeText(requireContext(), "Izinkan penyimpanan dulu", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        Toast.makeText(requireContext(), "Mengambil data $type...", Toast.LENGTH_SHORT).show()
//
//        val collection = when (type) {
//            "users" -> "users"
//            "payments" -> "payments"
//            "activities" -> "activities"
//            else -> null
//        }
//
//        if (collection == null) {
//            Toast.makeText(requireContext(), "Tipe laporan tidak dikenal", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        firestore.collection(collection).get()
//            .addOnSuccessListener { querySnapshot ->
//                if (querySnapshot.isEmpty) {
//                    Toast.makeText(requireContext(), "Tidak ada data untuk $type", Toast.LENGTH_SHORT).show()
//                    return@addOnSuccessListener
//                }
//
//                // Buat PDF document
//                val pdfDocument = PdfDocument()
//                val paint = Paint()
//                paint.textSize = 12f
//
//                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
//                val page = pdfDocument.startPage(pageInfo)
//                val canvas: Canvas = page.canvas
//
//                var y = 60
//                paint.isFakeBoldText = true
//                canvas.drawText("Laporan Data ${type.uppercase()}", 180f, y.toFloat(), paint)
//                paint.isFakeBoldText = false
//                y += 30
//
//                for ((index, doc) in querySnapshot.documents.withIndex()) {
//                    val data = doc.data ?: continue
//                    canvas.drawText("${index + 1}. ${doc.id}", 40f, y.toFloat(), paint)
//                    y += 20
//                    for ((key, value) in data) {
//                        canvas.drawText("   • $key: $value", 60f, y.toFloat(), paint)
//                        y += 20
//
//                        // Jika halaman hampir penuh → buat halaman baru
//                        if (y > 780) {
//                            pdfDocument.finishPage(page)
//                            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
//                            val newPage = pdfDocument.startPage(newPageInfo)
//                            val newCanvas = newPage.canvas
//                            y = 60
//                            newCanvas.drawText("Halaman Lanjutan (${pdfDocument.pages.size})", 40f, y.toFloat(), paint)
//                            y += 30
//                        }
//                    }
//                    y += 10
//                }
//
//                pdfDocument.finishPage(page)
//
//                // Simpan file PDF ke folder Download
//                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//                val fileName = "Report_${type}_${timestamp}.pdf"
//                val filePath = File(
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//                    fileName
//                )
//
//                try {
//                    pdfDocument.writeTo(FileOutputStream(filePath))
//                    pdfDocument.close()
//                    Toast.makeText(
//                        requireContext(),
//                        "Laporan tersimpan di Download/$fileName",
//                        Toast.LENGTH_LONG
//                    ).show()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    Toast.makeText(
//                        requireContext(),
//                        "Gagal menyimpan PDF: ${e.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(requireContext(), "Gagal mengambil data: ${it.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun loadAdminProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
        try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Load admin data from Firestore
                    val adminDoc = firestore.collection("admins")
                        .document(currentUser.uid)
                        .get()
                        .await()

                    if (adminDoc.exists()) {
                        val name = adminDoc.getString("name") ?: "Admin"
                        val email = adminDoc.getString("email") ?: currentUser.email ?: ""
                        val phone = adminDoc.getString("phone") ?: "+62 812 3456 7890"
                        val role = adminDoc.getString("role") ?: "Administrator"
                        val photoBase64 = adminDoc.getString("photoBase64")
                        val createdAt = adminDoc.getTimestamp("createdAt")?.toDate()
                        val lastLogin = adminDoc.getTimestamp("lastLogin")?.toDate()

                        // Update UI
                        binding.tvAdminName.text = name
                        binding.tvAdminEmail.text = email
                        binding.tvAdminPhone.text = if (phone.isNotEmpty()) phone else "+62 812 3456 7890"
                        binding.tvAdminRole.text = role
                        if (!photoBase64.isNullOrEmpty()) {
                            try {
                                val imageBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(
                                    imageBytes, 0, imageBytes.size
                                )

                                Glide.with(this@AdminProfileFragment)
                                    .load(bitmap)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_admin_avatar)
                                    .into(binding.ivAdminPhoto)

                            } catch (e: Exception) {
                                binding.ivAdminPhoto.setImageResource(R.drawable.ic_admin_avatar)
                            }
                        }


                        // Format dates
//                        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))

//                        if (createdAt != null) {
//                            binding.tvJoinDate.text = "Joined: ${dateFormat.format(createdAt)}"
//                        }
//
//                        if (lastLogin != null) {
//                            binding.tvLastLogin.text = "Last login: ${dateFormat.format(lastLogin)}"
//                        } else {
//                            binding.tvLastLogin.text = "Last login: Just now"
//                        }

                    } else {
                        // Create admin profile if not exists
                        createAdminProfile(currentUser.email ?: "")
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun createAdminProfile(email: String) {
        try {
            val currentUser = auth.currentUser ?: return

            val adminData = hashMapOf(
                "name" to "Administrator",
                "email" to email,
                "phone" to "",
                "role" to "Super Admin",
                "photoBase64" to "",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "lastLogin" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("admins")
                .document(currentUser.uid)
                .set(adminData)
                .await()

            // Reload profile
            loadAdminProfile()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error creating profile: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAdminStats() {
        lifecycleScope.launch {
            try {
                // Load admin activity stats
                val totalUsers = firestore.collection("users").get().await().size()
                val totalDiseases = firestore.collection("cattle_diseases").get().await().size()
                val totalNotifications = firestore.collection("notifications").get().await().size()

                // Calculate today's activity
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)

                val todayScans = firestore.collection("scan_history")
                    .whereGreaterThan("scanDate", com.google.firebase.Timestamp(today.time))
                    .get()
                    .await()
                    .size()

                // Update stats UI
                binding.tvTotalUsers.text = totalUsers.toString()
                binding.tvTotalDiseases.text = totalDiseases.toString()
                binding.tvTotalNotifications.text = totalNotifications.toString()
                binding.tvTodayScans.text = todayScans.toString()

            } catch (e: Exception) {
                // Handle error silently
                binding.tvTotalUsers.text = "0"
                binding.tvTotalDiseases.text = "0"
                binding.tvTotalNotifications.text = "0"
                binding.tvTodayScans.text = "0"
            }
        }
    }

    private fun editProfile() {
        val dialog = EditAdminProfileDialog.newInstance { name, phone ->
            updateAdminProfile(name, phone)
        }
        dialog.show(parentFragmentManager, "EditAdminProfileDialog")
    }

    private fun updateAdminProfile(name: String, phone: String) {
        lifecycleScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                val updates = hashMapOf<String, Any>(
                    "name" to name,
                    "phone" to phone,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("admins")
                    .document(currentUser.uid)
                    .update(updates)
                    .await()

                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                loadAdminProfile() // Refresh

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changePassword() {
        val dialog = ChangePasswordDialog.newInstance { oldPassword, newPassword ->
            updatePassword(oldPassword, newPassword)
        }
        dialog.show(parentFragmentManager, "ChangePasswordDialog")
    }

    private fun updatePassword(oldPassword: String, newPassword: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                // Re-authenticate user with old password
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                    currentUser.email ?: "", oldPassword
                )

                currentUser.reauthenticate(credential).await()

                // Update password
                currentUser.updatePassword(newPassword).await()

                Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error changing password: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeProfilePhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }


    private fun openSettings() {
        val intent = Intent(requireContext(), AdminSettingsActivity::class.java)
        startActivity(intent)
    }


    private fun logout() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout from admin panel?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // Update last login time
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    firestore.collection("admins")
                        .document(currentUser.uid)
                        .update("lastLogin", com.google.firebase.Timestamp.now())
                        .await()
                }

                // Sign out
                auth.signOut()

                // Navigate back to login
                requireActivity().finish()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error during logout: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
