package com.example.sadamoo.users

import android.R.attr.description
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.sadamoo.R
import com.example.sadamoo.databinding.ActivityScanResultBinding
import com.example.sadamoo.users.data.Detection
import com.example.sadamoo.users.data.DetectionRoomDatabase
import com.example.sadamoo.users.data.ScanHistory
import com.example.sadamoo.users.helper.Classifier
import com.example.sadamoo.users.models.DiseaseInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ScanResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanResultBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var imagePath: String? = null
    private var resultType: String = ""
    private var displayName: String = ""
    private var confidence: Float = 0f
    private var isHealthy: Boolean = true
    private var detectedDisease: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        imagePath = intent.getStringExtra("imagePath")
        resultType = intent.getStringExtra("resultType") ?: "undetected" // BARU
        displayName = intent.getStringExtra("displayName") ?: "Tidak Dapat Mendeteksi" // BARU
        confidence = intent.getFloatExtra("confidence", 0f)
        isHealthy = intent.getBooleanExtra("isHealthy", false)
        detectedDisease = intent.getStringExtra("detectedDisease")

        // Display image
        displayScannedImage()

        // Setup scan results with disease detection
        setupEnhancedScanResults()

        // Bottom navigation
        setupBottomNavigation()

        // Auto-save scan result
        val isFromHistory = intent.getBooleanExtra("is_from_history", false)
        if (!isFromHistory) {
            saveToDatabase()
            saveToFirestore()
        }
    }

    private fun displayScannedImage() {
        if (imagePath != null) {
            val imageFile = File(imagePath!!)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                binding.ivScannedImage.setImageBitmap(bitmap)
            }
        }
    }

    private fun setupEnhancedScanResults() {
        binding.tvCattleType.text = displayName
        binding.tvConfidence.text = "${"%.1f".format(confidence * 100)}%"

        when (resultType) {
            "undetected" -> setupUndetectedResult()
            "cattle_type" -> setupHealthyResult()
            "disease" -> setupDiseaseResult(displayName)
            else -> setupUndetectedResult()
        }
    }


    private fun detectDiseaseFromResult(result: String): String? {
        return when {
            result.contains("lumpy", ignoreCase = true) ||
                    result.contains("LSD", ignoreCase = true) -> "LSD"

            result.contains("cacingan", ignoreCase = true) ||
                    result.contains("worm", ignoreCase = true) -> "Cacingan"

            result.contains("PMK", ignoreCase = true) ||
                    result.contains("foot", ignoreCase = true) ||
                    result.contains("mouth", ignoreCase = true) -> "PMK"

            else -> null
        }
    }

    private fun setupHealthyResult() {
        binding.apply {
            tvHealthStatus.text = "🟢 Sapi Anda Sehat"
            tvHealthStatus.setTextColor(ContextCompat.getColor(this@ScanResultActivity, R.color.green_dark))

            // Tambahkan info jenis sapi dari description.json
            val classifier = Classifier(this@ScanResultActivity)
            val cattleInfo = classifier.getDiseaseInfoByName(displayName)

            if (cattleInfo != null) {
                tvDiseaseDescription.text = "${cattleInfo.description}\n}"
                tvDiseaseDescription.visibility = View.VISIBLE
            }

            layoutDiseaseInfo.visibility = View.GONE
        }
    }

    private fun setupUndetectedResult() {
        binding.apply {
            tvHealthStatus.text = "⚪ Tidak Dapat Mendeteksi"
            tvHealthStatus.setTextColor(ContextCompat.getColor(this@ScanResultActivity, R.color.gray_dark))

            // Sembunyikan info penyakit
            layoutDiseaseInfo.visibility = View.GONE

            // Tambahkan pesan penjelasan
            tvDiseaseDescription.text = "Sistem tidak dapat mengenali kondisi sapi dari hasil gambar. Silakan coba ambil foto dengan pencahayaan yang lebih baik atau dari sudut yang jelas."
            tvDiseaseDescription.visibility = View.VISIBLE
        }
    }

    private fun setupDiseaseResult(disease: String) {
        lifecycleScope.launch {
            try {
                val diseaseInfo = getDiseaseInfo(disease)
                displayDiseaseInformation(diseaseInfo)
            } catch (e: Exception) {
                val localDiseaseInfo = getLocalDiseaseInfo(disease)
                displayDiseaseInformation(localDiseaseInfo)
            }
        }
    }

    private suspend fun getDiseaseInfo(diseaseName: String): DiseaseInfo {
        val diseaseDoc = firestore.collection("cattle_diseases")
            .whereEqualTo("name", diseaseName)
            .limit(1)
            .get()
            .await()

        if (!diseaseDoc.isEmpty) {
            val doc = diseaseDoc.documents[0]
            return DiseaseInfo(
                name = doc.getString("name") ?: diseaseName,
                scientificName = doc.getString("scientificName") ?: "",
                description = doc.getString("description") ?: "",
                symptoms = doc.get("symptoms") as? List<String> ?: emptyList(),
                causes = doc.get("causes") as? List<String> ?: emptyList(),
                treatments = doc.get("treatments") as? List<String> ?: emptyList(),
                prevention = doc.get("prevention") as? List<String> ?: emptyList(),
                estimatedLoss = doc.getLong("estimatedLoss")?.toInt() ?: 0,
                severity = doc.getString("severity") ?: "moderate",
                contagious = doc.getBoolean("contagious") ?: false
            )
        } else {
            return getLocalDiseaseInfo(diseaseName)
        }
    }

    private fun getLocalDiseaseInfo(disease: String): DiseaseInfo {
        return when (disease.uppercase()) {
            "LSD" -> DiseaseInfo(
                name = "Lumpy Skin Disease (LSD)",
                scientificName = "Capripoxvirus",
                description = "Penyakit kulit menular pada sapi yang disebabkan oleh virus capripox, ditandai dengan benjolan-benjolan pada kulit.",
                symptoms = listOf(
                    "Benjolan keras pada kulit (nodules)",
                    "Demam tinggi (40-41°C)",
                    "Penurunan nafsu makan",
                    "Air liur berlebihan",
                    "Pembengkakan kelenjar getah bening",
                    "Penurunan produksi susu"
                ),
                causes = listOf(
                    "Virus Capripox",
                    "Penularan melalui serangga (lalat, nyamuk, kutu)",
                    "Kontak langsung dengan hewan terinfeksi",
                    "Kontaminasi pakan dan air minum"
                ),
                treatments = listOf(
                    "Isolasi hewan yang terinfeksi",
                    "Pemberian antibiotik untuk mencegah infeksi sekunder",
                    "Perawatan luka dengan antiseptik",
                    "Pemberian vitamin dan mineral",
                    "Konsultasi dengan dokter hewan"
                ),
                prevention = listOf(
                    "Vaksinasi rutin",
                    "Kontrol serangga vektor",
                    "Karantina hewan baru",
                    "Sanitasi kandang yang baik"
                ),
                estimatedLoss = 15000000,
                severity = "severe",
                contagious = true
            )

            "CACINGAN" -> DiseaseInfo(
                name = "Cacingan (Helminthiasis)",
                scientificName = "Various helminths",
                description = "Infeksi parasit cacing pada saluran pencernaan sapi yang dapat menurunkan produktivitas.",
                symptoms = listOf(
                    "Diare kronis",
                    "Penurunan berat badan",
                    "Bulu kusam dan kasar",
                    "Anemia (pucat pada selaput lendir)",
                    "Perut buncit",
                    "Nafsu makan menurun"
                ),
                causes = listOf(
                    "Cacing gelang (Ascaris)",
                    "Cacing pita (Taenia)",
                    "Cacing hati (Fasciola)",
                    "Lingkungan kandang yang kotor",
                    "Air minum terkontaminasi"
                ),
                treatments = listOf(
                    "Pemberian obat cacing (anthelmintic)",
                    "Albendazole atau Fenbendazole",
                    "Ivermectin untuk cacing tertentu",
                    "Perbaikan nutrisi",
                    "Pemberian vitamin dan mineral"
                ),
                prevention = listOf(
                    "Sanitasi kandang yang baik",
                    "Pemberian pakan berkualitas",
                    "Air minum yang bersih",
                    "Rotasi padang penggembalaan",
                    "Pemeriksaan feses berkala"
                ),
                estimatedLoss = 5000000,
                severity = "moderate",
                contagious = false
            )

            "PMK" -> DiseaseInfo(
                name = "Foot and Mouth Disease (PMK)",
                scientificName = "Aphthovirus",
                description = "Penyakit virus yang sangat menular pada hewan berkuku genap, menyerang mulut, kaki, dan ambing.",
                symptoms = listOf(
                    "Lepuh pada mulut, lidah, dan gusi",
                    "Lepuh pada kaki dan sela-sela kuku",
                    "Demam tinggi",
                    "Air liur berlebihan",
                    "Kesulitan makan dan minum",
                    "Pincang"
                ),
                causes = listOf(
                    "Virus Foot and Mouth Disease",
                    "Penularan melalui udara",
                    "Kontak langsung dengan hewan terinfeksi",
                    "Kontaminasi pakan, air, dan peralatan",
                    "Manusia sebagai pembawa virus"
                ),
                treatments = listOf(
                    "Isolasi ketat hewan terinfeksi",
                    "Perawatan suportif",
                    "Pembersihan luka dengan larutan antiseptik",
                    "Pemberian pakan lunak",
                    "Terapi cairan dan elektrolit"
                ),
                prevention = listOf(
                    "Vaksinasi sesuai program pemerintah",
                    "Biosekuriti ketat",
                    "Karantina dan pemeriksaan hewan",
                    "Desinfeksi kendaraan dan peralatan",
                    "Pembatasan lalu lintas hewan"
                ),
                estimatedLoss = 25000000,
                severity = "severe",
                contagious = true
            )

            else -> DiseaseInfo(
                name = "Penyakit Tidak Dikenal",
                scientificName = "",
                description = "Penyakit yang terdeteksi belum terdaftar dalam database.",
                symptoms = emptyList(),
                causes = emptyList(),
                treatments = listOf("Konsultasi dengan dokter hewan"),
                prevention = emptyList(),
                estimatedLoss = 0,
                severity = "unknown",
                contagious = false
            )
        }
    }

    private fun displayDiseaseInformation(diseaseInfo: DiseaseInfo) {
        binding.apply {
            // Health Status
            tvHealthStatus.text = "🔴 Terdeteksi: ${diseaseInfo.name}"
            tvHealthStatus.setTextColor(ContextCompat.getColor(this@ScanResultActivity, R.color.red_dark))

            // Show disease information layout
            layoutDiseaseInfo.visibility = View.VISIBLE

            // Disease name and scientific name
            tvDiseaseName.text = diseaseInfo.name
            tvScientificName.text = diseaseInfo.scientificName
            tvDiseaseDescription.text = diseaseInfo.description

            // Severity and contagious status
            tvSeverity.text = getSeverityText(diseaseInfo.severity)
            tvSeverity.setTextColor(getSeverityColor(diseaseInfo.severity))

            tvContagiousStatus.text = if (diseaseInfo.contagious) "⚠️ Menular" else "✅ Tidak Menular"
            tvContagiousStatus.setTextColor(
                if (diseaseInfo.contagious)
                    ContextCompat.getColor(this@ScanResultActivity, R.color.red_dark)
                else
                    ContextCompat.getColor(this@ScanResultActivity, R.color.green_dark)
            )

            // Symptoms
            tvSymptoms.text = diseaseInfo.symptoms.joinToString("\n") { "• $it" }

            // Causes
            tvCauses.text = diseaseInfo.causes.joinToString("\n") { "• $it" }

            // Treatments
            tvTreatments.text = diseaseInfo.treatments.joinToString("\n") { "• $it" }

            // Prevention
            tvPrevention.text = diseaseInfo.prevention.joinToString("\n") { "• $it" }

            // Economic loss
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            tvEstimatedLoss.text = formatter.format(diseaseInfo.estimatedLoss).replace("IDR", "Rp")

            // Set background colors based on severity
            setDiseaseCardColors(diseaseInfo.severity)
        }
    }

    private fun getSeverityText(severity: String): String {
        return when (severity) {
            "mild" -> "Ringan"
            "moderate" -> "Sedang"
            "severe" -> "Berat"
            else -> "Tidak Diketahui"
        }
    }

    private fun getSeverityColor(severity: String): Int {
        return when (severity) {
            "mild" -> ContextCompat.getColor(this, R.color.green_dark)
            "moderate" -> ContextCompat.getColor(this, R.color.orange_dark)
            "severe" -> ContextCompat.getColor(this, R.color.red_dark)
            else -> ContextCompat.getColor(this, R.color.gray_dark)
        }
    }

    private fun setDiseaseCardColors(severity: String) {
        val cardBackground = when (severity) {
            "mild" -> R.color.green_light
            "moderate" -> R.color.orange_light
            "severe" -> R.color.red_light
            else -> R.color.gray_light
        }

//        binding.layoutDiseaseInfo.setBackgroundColor(
//            ContextCompat.getColor(this, cardBackground)
//        )
    }

    private fun setupBottomNavigation() {
        binding.navBack.setOnClickListener {
            navigateToHistory()
        }

        binding.navSave.setOnClickListener {
            Toast.makeText(this, "Hasil scan disimpan ke riwayat", Toast.LENGTH_SHORT).show()
            saveToDatabase()
        }

        binding.navConsultation.setOnClickListener {
            val intent = Intent(this, ChatConsultationActivity::class.java).apply {
                putExtra("doctor_name", "Dr. Ahmad Veteriner")
                putExtra("consultation_id", "new_consultation")
                putExtra("detected_disease", detectedDisease)
                putExtra("cattle_type", resultType)
                putExtra("confidence", confidence)
            }
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToHistory()
    }

    private fun navigateToHistory() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "history")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun saveToDatabase() {
        val detectionDao = DetectionRoomDatabase.getDatabase(applicationContext).detectionDao()
        val scanHistoryDao = DetectionRoomDatabase.getDatabase(applicationContext).scanHistoryDao()

        lifecycleScope.launch {
            try {

                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid ?: "anonymous"

                // Simpan ke Detection
                val detection = Detection(
                    userId = userId,
                    imagePath = imagePath ?: "",
                    cattleType = when (resultType) {
                        "cattle_type" -> displayName
                        "disease" -> "Terdeteksi Penyakit"
                        else -> "Tidak Dapat Mendeteksi"
                    },
                    description = displayName,
                    confidence = confidence,
                    isHealthy = isHealthy,
                    detectedDisease = detectedDisease,
                    timestamp = Date()
                )
                detectionDao.insert(detection)

                // Simpan ke ScanHistory
                val statusText = when (resultType) {
                    "cattle_type" -> "Sehat - $displayName"
                    "disease" -> "Terdeteksi: $displayName"
                    else -> "Tidak Dapat Mendeteksi"
                }

                val scanHistory = ScanHistory(
                    imagePath = imagePath ?: "",
                    result = displayName,
                    confidence = confidence,
                    timestamp = Date(),
                    diseaseInfo = statusText
                )
                scanHistoryDao.insert(scanHistory)

            } catch (e: Exception) {
                Toast.makeText(
                    this@ScanResultActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveToFirestore() {
        lifecycleScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                val scanData = hashMapOf(
                    "userId" to currentUser.uid,
                    "resultType" to resultType,
                    "displayName" to displayName,
                    "confidence" to confidence,
                    "isHealthy" to isHealthy,
                    "detectedDisease" to if (isHealthy) "Sehat" else detectedDisease,
                    "imagePath" to imagePath,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("scan_history")
                    .add(scanData)
                    .await()

            } catch (e: Exception) {
                Toast.makeText(
                    this@ScanResultActivity,
                    "Firestore error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
