package com.example.sadamoo.users.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sadamoo.databinding.DialogPaymentBinding
//import com.example.sadamoo.users.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PaymentDialogFragment : DialogFragment() {
    private lateinit var binding: DialogPaymentBinding
    private lateinit var packageName: String
    private var price: Int = 0

    private var packageDuration: Int = 1

    private var selectedImageUri: Uri? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                // Show selected image
                Glide.with(this)
                    .load(uri)
                    .into(binding.ivPaymentProof)

                binding.ivPaymentProof.visibility = View.VISIBLE
                binding.tvUploadHint.text = "✅ Bukti pembayaran telah dipilih"
                binding.btnUploadProof.text = "Ganti Bukti"
                binding.btnSubmitPayment.isEnabled = true
            }
        }
    }

    companion object {
        fun newInstance(
            packageName: String,
            price: Int,
            duration: Int = 1
        ): PaymentDialogFragment {
            val fragment = PaymentDialogFragment()
            val args = Bundle()
            args.putString("package_name", packageName)
            args.putInt("price", price)
            args.putInt("duration", duration)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            packageName = it.getString("package_name") ?: ""
            price = it.getInt("price", 0)
            packageDuration = it.getInt("duration", 1)
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPaymentSummary()
        setupBankInfo()
        setupButtons()
    }

    private fun setupPaymentSummary() {
        binding.apply {
            tvPackageName.text = "Paket $packageName"

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val formattedPrice = formatter.format(price).replace("IDR", "Rp").replace(",00", "")
            tvPackagePrice.text = formattedPrice

            tvPackageDuration.text = getDurationText()
            tvPaymentCode.text = generatePaymentCode()

            val adminFee = 2500
            val total = price + adminFee

            tvAdminFee.text = formatter.format(adminFee).replace("IDR", "Rp").replace(",00", "")
            tvTotalAmount.text = formatter.format(total).replace("IDR", "Rp").replace(",00", "")
        }
    }

    private fun getDurationText(): String {
        return when (packageDuration) {
            1 -> "1 Bulan"
            3 -> "3 Bulan"
            6 -> "6 Bulan"
            12 -> "1 Tahun"
            else -> "$packageDuration Bulan"
        }
    }

    private fun generatePaymentCode(): String {
        return "SADA${System.currentTimeMillis().toString().takeLast(6)}"
    }

    private fun setupBankInfo() {
        binding.apply {
            // Bank info (bisa diambil dari Firebase config)
            tvBankName.text = "Bank BCA"
            tvAccountNumber.text = "1234567890"
            tvAccountName.text = "PT. SADA MOO INDONESIA"
            tvPaymentMethod.text = "Bank Transfer" // 🔥 tambahkan TextView di layout kamu

            btnCopyAccount.setOnClickListener {
                copyToClipboard(binding.tvAccountNumber.text.toString())
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Account Number", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Nomor rekening disalin!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtons() {
        binding.btnUploadProof.setOnClickListener {
            selectPaymentProof()
        }

        binding.btnSubmitPayment.setOnClickListener {
            submitPayment()
        }

        binding.btnCancelPayment.setOnClickListener {
            dismiss()
        }
    }

    private fun selectPaymentProof() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun submitPayment() {
        if (selectedImageUri == null) {
            Toast.makeText(
                requireContext(),
                "Silakan upload bukti pembayaran terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        binding.btnSubmitPayment.isEnabled = false
        binding.btnSubmitPayment.text = "Mengirim..."
        binding.progressUpload.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Check if user is logged in
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    throw Exception("Please login first")
                }

                // Upload payment proof to Cloudinary
                val paymentProofUrl = uploadPaymentProofToCloudinary()

                if (paymentProofUrl == null) {
                    throw Exception("Failed to upload image to Cloudinary")
                }

                // Create payment record
                createPaymentRecord(paymentProofUrl)

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("network") == true -> "Network error. Check your connection."
                    e.message?.contains("Cloudinary") == true -> "Image upload failed. Please try again."
                    e.message?.contains("timeout") == true -> "Upload timeout. Please try again."
                    else -> "Error: ${e.message}"
                }

                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()

            } finally {
                binding.btnSubmitPayment.isEnabled = true
                binding.btnSubmitPayment.text = "Kirim Bukti Pembayaran"
                binding.progressUpload.visibility = View.GONE
            }
        }
    }

    // 🔥 NEW: Upload to Cloudinary instead of Firebase Storage
    private suspend fun uploadPaymentProofToCloudinary(): String? {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not logged in")

            // Add user info to the upload for better organization
            val publicId = "payment_proofs/payment_${currentUser.uid}_${System.currentTimeMillis()}"

            uploadToCloudinary(requireContext(), selectedImageUri!!, publicId)
        } catch (e: Exception) {
            Log.e("PaymentUpload", "Cloudinary upload failed: ${e.message}", e)
            null
        }
    }

    // 🔥 Enhanced Cloudinary Upload Function
    private suspend fun uploadToCloudinary(
        context: Context,
        imageUri: Uri,
        publicId: String? = null
    ): String? =
        withContext(Dispatchers.IO) {
            try {
                val cloudName = "dpxz2favg"
                val uploadPreset = "sadamoo"

                val inputStream = context.contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes() ?: return@withContext null
                inputStream.close()

                val requestBodyBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", "payment_proof.jpg",
                        imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                    )
                    .addFormDataPart("upload_preset", uploadPreset)
                    .addFormDataPart("folder", "sadamoo/payment_proofs") // Organize in folders

                // Add custom public_id if provided
                if (publicId != null) {
                    requestBodyBuilder.addFormDataPart("public_id", publicId)
                }

                // Add tags for better organization
                requestBodyBuilder.addFormDataPart("tags", "payment_proof,sadamoo,mobile_app")

                val requestBody = requestBodyBuilder.build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .post(requestBody)
                    .build()

                // Create OkHttpClient with timeout settings
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("Cloudinary", "Upload failed with code: ${response.code}")
                    Log.e("Cloudinary", "Response body: ${response.body?.string()}")
                    return@withContext null
                }

                val responseBody = response.body?.string() ?: return@withContext null
                val jsonObject = JSONObject(responseBody)
                val imageUrl = jsonObject.getString("secure_url")

                Log.d("Cloudinary", "Successfully uploaded image URL: $imageUrl")
                Log.d("Cloudinary", "Public ID: ${jsonObject.optString("public_id")}")
                Log.d("Cloudinary", "Format: ${jsonObject.optString("format")}")
                Log.d("Cloudinary", "Size: ${jsonObject.optInt("bytes")} bytes")

                return@withContext imageUrl

            } catch (e: Exception) {
                Log.e("Cloudinary", "Upload failed: ${e.message}", e)
                return@withContext null
            }
        }

    private suspend fun createPaymentRecord(paymentProofUrl: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")

        val calendar = Calendar.getInstance()
        when (packageName) {
            "Basic" -> calendar.add(Calendar.MONTH, packageDuration)
            "Standard" -> calendar.add(Calendar.MONTH, packageDuration)
            "Premium" -> calendar.add(Calendar.MONTH, packageDuration)
            "Ultimate" -> calendar.add(Calendar.MONTH, packageDuration)
        }

        val cleanPrice = price
        val adminFee = 2500
        val totalAmount = cleanPrice + adminFee

        val paymentData = hashMapOf(
            "userId" to currentUser.uid,
            "userName" to (getCurrentUserName() ?: "Unknown"),
            "userEmail" to (currentUser.email ?: ""),
            "packageType" to packageName,
            "packageDuration" to packageDuration,

            "amount" to cleanPrice,
            "originalAmount" to cleanPrice,
            "adminFee" to adminFee,
            "totalAmount" to totalAmount,

            "status" to "pending", // pending, completed, failed, refunded, rejected
            "paymentMethod" to "Bank Transfer", // 🔥 atau sesuaikan dengan pilihan user
            "paymentCode" to binding.tvPaymentCode.text.toString(),
            "paymentProofUrl" to paymentProofUrl,

            "bankAccount" to binding.tvAccountNumber.text.toString(),
            "notes" to "Menunggu verifikasi admin",

            "transactionDate" to com.google.firebase.Timestamp.now(), // 🔥 ini dibaca admin
            "submittedAt" to com.google.firebase.Timestamp.now(),

            "verifiedAt" to null,
            "verifiedBy" to null,

            "subscriptionStartDate" to null,
            "subscriptionEndDate" to com.google.firebase.Timestamp(calendar.time)
        )

        firestore.collection("payments")
            .add(paymentData)
            .await()

        Toast.makeText(
            requireContext(),
            "Bukti pembayaran berhasil dikirim!\nMohon tunggu verifikasi admin (1x24 jam)",
            Toast.LENGTH_LONG
        ).show()

        dismiss()
//        (activity as? ProfileActivity)?.loadUserProfile()
    }

    private suspend fun getCurrentUserName(): String? {
        return try {
            val currentUser = auth.currentUser ?: return null
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            userDoc.getString("name")
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
