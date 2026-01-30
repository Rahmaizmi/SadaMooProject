package com.example.sadamoo.admin.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.R
import com.example.sadamoo.admin.adapters.DiseaseAdapter
import com.example.sadamoo.admin.models.DiseaseModel
import com.example.sadamoo.admin.dialogs.AddEditDiseaseDialog
import com.example.sadamoo.admin.dialogs.DiseaseDetailDialog
import com.example.sadamoo.databinding.FragmentDiseaseManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class DiseaseManagementFragment : Fragment() {
    private var _binding: FragmentDiseaseManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var diseaseAdapter: DiseaseAdapter
    private var allDiseases = mutableListOf<DiseaseModel>()
    private var filteredDiseases = mutableListOf<DiseaseModel>()
    private var currentFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiseaseManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupButtons()
        loadDiseases()
    }

    private fun setupRecyclerView() {
        diseaseAdapter = DiseaseAdapter(
            diseases = filteredDiseases,
            onDiseaseClick = { disease -> showDiseaseDetail(disease) },
            onEditDisease = { disease -> editDisease(disease) },
            onToggleStatus = { disease -> toggleDiseaseStatus(disease) },
            onDeleteDisease = { disease -> deleteDisease(disease) }
        )

        binding.rvDiseases.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = diseaseAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                filterDiseases(query, currentFilter)
            }
        })
    }

    private fun setupFilters() {
        setActiveFilter("all")

        binding.btnFilterAll.setOnClickListener {
            setActiveFilter("all")
            filterDiseases(binding.etSearch.text.toString(), "all")
        }

        binding.btnFilterMild.setOnClickListener {
            setActiveFilter("mild")
            filterDiseases(binding.etSearch.text.toString(), "mild")
        }

        binding.btnFilterModerate.setOnClickListener {
            setActiveFilter("moderate")
            filterDiseases(binding.etSearch.text.toString(), "moderate")
        }

        binding.btnFilterSevere.setOnClickListener {
            setActiveFilter("severe")
            filterDiseases(binding.etSearch.text.toString(), "severe")
        }
    }

    private fun setActiveFilter(filter: String) {
        currentFilter = filter

        val inactiveBg = R.drawable.filter_button_inactive
        val activeBg = R.drawable.filter_button_active

        val inactiveTextColor = requireContext().getColor(R.color.blue_primary)
        val activeTextColor = requireContext().getColor(android.R.color.white)

        // Reset semua tombol
        val buttons = listOf(
            binding.btnFilterAll,
            binding.btnFilterMild,
            binding.btnFilterModerate,
            binding.btnFilterSevere
        )

        buttons.forEach {
            it.background = requireContext().getDrawable(inactiveBg)
            it.setTextColor(inactiveTextColor)
        }

        // Aktifkan tombol sesuai filter
        when (filter) {
            "all" -> {
                binding.btnFilterAll.background = requireContext().getDrawable(activeBg)
                binding.btnFilterAll.setTextColor(activeTextColor)
            }
            "mild" -> {
                binding.btnFilterMild.background = requireContext().getDrawable(activeBg)
                binding.btnFilterMild.setTextColor(activeTextColor)
            }
            "moderate" -> {
                binding.btnFilterModerate.background = requireContext().getDrawable(activeBg)
                binding.btnFilterModerate.setTextColor(activeTextColor)
            }
            "severe" -> {
                binding.btnFilterSevere.background = requireContext().getDrawable(activeBg)
                binding.btnFilterSevere.setTextColor(activeTextColor)
            }
        }
    }


    private fun setupButtons() {
        binding.btnAddDisease.setOnClickListener {
            addNewDisease()
        }

//        binding.btnBack.setOnClickListener {
//            parentFragmentManager.popBackStack()
//        }
    }

    private fun loadDiseases() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                // Load diseases from Firebase
                val diseasesSnapshot = firestore.collection("cattle_diseases")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                allDiseases.clear()

                if (diseasesSnapshot.isEmpty) {
                    // Create default diseases if none exist
                    createDefaultDiseases()
                } else {
                    // Load existing diseases
                    for (document in diseasesSnapshot.documents) {
                        val disease = DiseaseModel(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            scientificName = document.getString("scientificName") ?: "",
                            description = document.getString("description") ?: "",
                            symptoms = document.get("symptoms") as? List<String> ?: emptyList(),
                            causes = document.get("causes") as? List<String> ?: emptyList(),
                            treatments = document.get("treatments") as? List<String> ?: emptyList(),
                            prevention = document.get("prevention") as? List<String> ?: emptyList(),
                            severity = document.getString("severity") ?: "moderate",
                            isActive = document.getBoolean("isActive") ?: true,
                            estimatedLoss = document.getLong("estimatedLoss")?.toInt() ?: 0,
                            recoveryTime = document.getString("recoveryTime") ?: "",
                            contagious = document.getBoolean("contagious") ?: false,
                            createdAt = document.getTimestamp("createdAt")?.toDate() ?: Date(),
                            updatedAt = document.getTimestamp("updatedAt")?.toDate() ?: Date(),
                            detectionCount = 0 // Will be calculated separately
                        )
                        allDiseases.add(disease)
                    }
                }

                // Load detection counts
                loadDetectionCounts()

                filteredDiseases.clear()
                filteredDiseases.addAll(allDiseases)
                diseaseAdapter.notifyDataSetChanged()

                updateDiseaseCount()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading diseases: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun createDefaultDiseases() {
        val defaultDiseases = listOf(
            DiseaseModel(
                id = "",
                name = "Lumpy Skin Disease (LSD)",
                scientificName = "Capripoxvirus",
                description = "Penyakit kulit menular pada sapi yang disebabkan oleh virus capripox, ditandai dengan benjolan-benjolan pada kulit.",
                symptoms = listOf(
                    "Benjolan keras pada kulit (nodules)",
                    "Demam tinggi (40-41°C)",
                    "Penurunan nafsu makan",
                    "Air liur berlebihan",
                    "Pembengkakan kelenjar getah bening",
                    "Penurunan produksi susu",
                    "Luka terbuka pada kulit"
                ),
                causes = listOf(
                    "Virus Capripox",
                    "Penularan melalui serangga (lalat, nyamuk, kutu)",
                    "Kontak langsung dengan hewan terinfeksi",
                    "Kontaminasi pakan dan air minum",
                    "Peralatan kandang yang terkontaminasi"
                ),
                treatments = listOf(
                    "Isolasi hewan yang terinfeksi",
                    "Pemberian antibiotik untuk mencegah infeksi sekunder",
                    "Perawatan luka dengan antiseptik",
                    "Pemberian vitamin dan mineral",
                    "Terapi suportif (cairan, elektrolit)",
                    "Konsultasi dengan dokter hewan"
                ),
                prevention = listOf(
                    "Vaksinasi rutin",
                    "Kontrol serangga vektor",
                    "Karantina hewan baru",
                    "Sanitasi kandang yang baik",
                    "Pembatasan pergerakan hewan",
                    "Pemeriksaan kesehatan berkala"
                ),
                severity = "severe",
                isActive = true,
                estimatedLoss = 15000000,
                recoveryTime = "2-4 minggu",
                contagious = true,
                createdAt = Date(),
                updatedAt = Date(),
                detectionCount = 0
            ),
            DiseaseModel(
                id = "",
                name = "Foot and Mouth Disease (PMK)",
                scientificName = "Aphthovirus",
                description = "Penyakit virus yang sangat menular pada hewan berkuku genap, menyerang mulut, kaki, dan ambing.",
                symptoms = listOf(
                    "Lepuh pada mulut, lidah, dan gusi",
                    "Lepuh pada kaki dan sela-sela kuku",
                    "Demam tinggi",
                    "Air liur berlebihan",
                    "Kesulitan makan dan minum",
                    "Pincang",
                    "Penurunan berat badan drastis"
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
                    "Terapi cairan dan elektrolit",
                    "Tidak ada pengobatan spesifik"
                ),
                prevention = listOf(
                    "Vaksinasi sesuai program pemerintah",
                    "Biosekuriti ketat",
                    "Karantina dan pemeriksaan hewan",
                    "Desinfeksi kendaraan dan peralatan",
                    "Pembatasan lalu lintas hewan",
                    "Pelaporan kasus mencurigakan"
                ),
                severity = "severe",
                isActive = true,
                estimatedLoss = 25000000,
                recoveryTime = "2-3 minggu",
                contagious = true,
                createdAt = Date(),
                updatedAt = Date(),
                detectionCount = 0
            ),
            DiseaseModel(
                id = "",
                name = "Cacingan (Helminthiasis)",
                scientificName = "Various helminths",
                description = "Infeksi parasit cacing pada saluran pencernaan sapi yang dapat menurunkan produktivitas.",
                symptoms = listOf(
                    "Diare kronis",
                    "Penurunan berat badan",
                    "Bulu kusam dan kasar",
                    "Anemia (pucat pada selaput lendir)",
                    "Perut buncit",
                    "Nafsu makan menurun",
                    "Pertumbuhan terhambat"
                ),
                causes = listOf(
                    "Cacing gelang (Ascaris)",
                    "Cacing pita (Taenia)",
                    "Cacing hati (Fasciola)",
                    "Lingkungan kandang yang kotor",
                    "Air minum terkontaminasi",
                    "Pakan yang tercemar telur cacing"
                ),
                treatments = listOf(
                    "Pemberian obat cacing (anthelmintic)",
                    "Albendazole atau Fenbendazole",
                    "Ivermectin untuk cacing tertentu",
                    "Perbaikan nutrisi",
                    "Pemberian vitamin dan mineral",
                    "Pengulangan pengobatan sesuai petunjuk"
                ),
                prevention = listOf(
                    "Sanitasi kandang yang baik",
                    "Pemberian pakan berkualitas",
                    "Air minum yang bersih",
                    "Rotasi padang penggembalaan",
                    "Pemeriksaan feses berkala",
                    "Pengobatan cacing rutin"
                ),
                severity = "moderate",
                isActive = true,
                estimatedLoss = 5000000,
                recoveryTime = "1-2 minggu",
                contagious = false,
                createdAt = Date(),
                updatedAt = Date(),
                detectionCount = 0
            ),
            DiseaseModel(
                id = "",
                name = "Mastitis",
                scientificName = "Various bacteria",
                description = "Peradangan pada kelenjar susu sapi yang dapat menurunkan kualitas dan kuantitas susu.",
                symptoms = listOf(
                    "Pembengkakan ambing",
                    "Ambing terasa panas dan nyeri",
                    "Susu berubah warna dan konsistensi",
                    "Adanya gumpalan dalam susu",
                    "Penurunan produksi susu",
                    "Demam pada kasus akut",
                    "Sapi tampak lesu"
                ),
                causes = listOf(
                    "Infeksi bakteri (Staphylococcus, Streptococcus)",
                    "Kebersihan kandang yang buruk",
                    "Teknik pemerahan yang salah",
                    "Luka pada puting susu",
                    "Stress pada sapi",
                    "Peralatan pemerahan yang kotor"
                ),
                treatments = listOf(
                    "Antibiotik intramammary",
                    "Antibiotik sistemik untuk kasus berat",
                    "Anti-inflamasi",
                    "Pemerahan teratur dan bersih",
                    "Kompres hangat pada ambing",
                    "Isolasi susu dari sapi sakit"
                ),
                prevention = listOf(
                    "Sanitasi peralatan pemerahan",
                    "Teknik pemerahan yang benar",
                    "Teat dipping setelah pemerahan",
                    "Lingkungan kandang yang bersih",
                    "Pemeriksaan rutin kualitas susu",
                    "Manajemen stress yang baik"
                ),
                severity = "moderate",
                isActive = true,
                estimatedLoss = 8000000,
                recoveryTime = "1-3 minggu",
                contagious = false,
                createdAt = Date(),
                updatedAt = Date(),
                detectionCount = 0
            )
        )

        for (disease in defaultDiseases) {
            val diseaseData = hashMapOf(
                "name" to disease.name,
                "scientificName" to disease.scientificName,
                "description" to disease.description,
                "symptoms" to disease.symptoms,
                "causes" to disease.causes,
                "treatments" to disease.treatments,
                "prevention" to disease.prevention,
                "severity" to disease.severity,
                "isActive" to disease.isActive,
                "estimatedLoss" to disease.estimatedLoss,
                "recoveryTime" to disease.recoveryTime,
                "contagious" to disease.contagious,
                "createdAt" to com.google.firebase.Timestamp(disease.createdAt),
                "updatedAt" to com.google.firebase.Timestamp(disease.updatedAt)
            )

            val docRef = firestore.collection("cattle_diseases").add(diseaseData).await()
            disease.id = docRef.id
            allDiseases.add(disease)
        }
    }

    private suspend fun loadDetectionCounts() {
        for (disease in allDiseases) {
            try {
                val detectionsSnapshot = firestore.collection("scan_history")
                    .whereEqualTo("diseaseDetected", disease.name)
                    .get()
                    .await()

                disease.detectionCount = detectionsSnapshot.size()
            } catch (e: Exception) {
                disease.detectionCount = 0
            }
        }
    }

    private fun filterDiseases(query: String, filter: String) {
        var filtered = allDiseases.toList()

        // Filter by severity
        when (filter) {
            "mild" -> filtered = filtered.filter { it.severity == "mild" }
            "moderate" -> filtered = filtered.filter { it.severity == "moderate" }
            "severe" -> filtered = filtered.filter { it.severity == "severe" }
        }

        // Filter by search query
        if (query.isNotEmpty()) {
            filtered = filtered.filter { disease ->
                disease.name.lowercase().contains(query) ||
                        disease.scientificName.lowercase().contains(query) ||
                        disease.description.lowercase().contains(query) ||
                        disease.symptoms.any { it.lowercase().contains(query) }
            }
        }

        filteredDiseases.clear()
        filteredDiseases.addAll(filtered)
        diseaseAdapter.notifyDataSetChanged()

        updateDiseaseCount()
    }

    private fun updateDiseaseCount() {
        binding.tvDiseaseCount.text = "${filteredDiseases.size} diseases found"
    }

    private fun addNewDisease() {
        val dialog = AddEditDiseaseDialog.newInstance(null) { disease ->
            saveDisease(disease)
        }
        dialog.show(parentFragmentManager, "AddDiseaseDialog")
    }

    private fun editDisease(disease: DiseaseModel) {
        val dialog = AddEditDiseaseDialog.newInstance(disease) { updatedDisease ->
            updateDisease(updatedDisease)
        }
        dialog.show(parentFragmentManager, "EditDiseaseDialog")
    }

    private fun saveDisease(disease: DiseaseModel) {
        lifecycleScope.launch {
            try {
                val diseaseData = hashMapOf(
                    "name" to disease.name,
                    "scientificName" to disease.scientificName,
                    "description" to disease.description,
                    "symptoms" to disease.symptoms,
                    "causes" to disease.causes,
                    "treatments" to disease.treatments,
                    "prevention" to disease.prevention,
                    "severity" to disease.severity,
                    "isActive" to disease.isActive,
                    "estimatedLoss" to disease.estimatedLoss,
                    "recoveryTime" to disease.recoveryTime,
                    "contagious" to disease.contagious,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("cattle_diseases")
                    .add(diseaseData)
                    .await()

                Toast.makeText(requireContext(), "Disease created successfully!", Toast.LENGTH_SHORT).show()
                loadDiseases() // Refresh data

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error creating disease: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDisease(disease: DiseaseModel) {
        lifecycleScope.launch {
            try {
                val diseaseData = hashMapOf(
                    "name" to disease.name,
                    "scientificName" to disease.scientificName,
                    "description" to disease.description,
                    "symptoms" to disease.symptoms,
                    "causes" to disease.causes,
                    "treatments" to disease.treatments,
                    "prevention" to disease.prevention,
                    "severity" to disease.severity,
                    "isActive" to disease.isActive,
                    "estimatedLoss" to disease.estimatedLoss,
                    "recoveryTime" to disease.recoveryTime,
                    "contagious" to disease.contagious,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("cattle_diseases").document(disease.id)
                    .update(diseaseData as Map<String, Any>)
                    .await()

                Toast.makeText(requireContext(), "Disease updated successfully!", Toast.LENGTH_SHORT).show()
                loadDiseases() // Refresh data

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating disease: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleDiseaseStatus(disease: DiseaseModel) {
        lifecycleScope.launch {
            try {
                val newStatus = !disease.isActive

                firestore.collection("cattle_diseases").document(disease.id)
                    .update("isActive", newStatus, "updatedAt", com.google.firebase.Timestamp.now())
                    .await()

                val statusText = if (newStatus) "activated" else "deactivated"
                Toast.makeText(requireContext(), "Disease ${disease.name} has been $statusText", Toast.LENGTH_SHORT).show()

                loadDiseases() // Refresh data

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating disease status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteDisease(disease: DiseaseModel) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Disease")
            .setMessage("Are you sure you want to delete '${disease.name}'?\n\nThis action cannot be undone and will affect the AI detection system.")
            .setPositiveButton("Delete") { _, _ ->
                performDeleteDisease(disease)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDeleteDisease(disease: DiseaseModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                firestore.collection("cattle_diseases").document(disease.id)
                    .delete()
                    .await()

                Toast.makeText(requireContext(), "Disease '${disease.name}' deleted successfully", Toast.LENGTH_SHORT).show()
                loadDiseases() // Refresh data

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error deleting disease: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDiseaseDetail(disease: DiseaseModel) {
        val dialog = DiseaseDetailDialog.newInstance(disease)
        dialog.show(parentFragmentManager, "DiseaseDetailDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
