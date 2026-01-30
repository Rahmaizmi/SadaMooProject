package com.example.sadamoo.users.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.R
import com.example.sadamoo.databinding.FragmentInformationBinding
import com.example.sadamoo.users.DiseaseDetailActivity
import com.example.sadamoo.users.adapters.DiseaseAdapter
import com.example.sadamoo.users.models.Disease

class InformationFragment : Fragment() {
    private var _binding: FragmentInformationBinding? = null
    private val binding get() = _binding!!

    private lateinit var diseaseAdapter: DiseaseAdapter
    private var allDiseases = listOf<Disease>()
    private var filteredDiseases = listOf<Disease>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDiseaseData()
        setupRecyclerView()
        setupSearch()
    }

    private fun setupDiseaseData() {
        allDiseases = listOf(
            Disease(
                id = "cacingan",
                name = "Cacingan",
                scientificName = "Helminthiasis",
                description = "Penyakit yang disebabkan oleh infeksi cacing parasit pada saluran pencernaan sapi yang dapat mengganggu penyerapan nutrisi dan pertumbuhan.",
                symptoms = listOf(
                    "Nafsu makan menurun",
                    "Berat badan turun drastis",
                    "Bulu kusam dan kasar",
                    "Diare berulang",
                    "Perut membesar (ascites)",
                    "Anemia (pucat pada selaput mata)",
                    "Pertumbuhan terhambat"
                ),
                causes = listOf(
                    "Konsumsi pakan atau air yang terkontaminasi telur cacing",
                    "Sanitasi kandang yang buruk",
                    "Kepadatan ternak yang tinggi",
                    "Sistem pemeliharaan yang tidak higienis",
                    "Kurangnya program deworming rutin"
                ),
                treatment = listOf(
                    "Pemberian obat cacing (antelmintik) sesuai dosis dokter hewan",
                    "Albendazole 10-15 mg/kg berat badan",
                    "Ivermectin 0.2 mg/kg berat badan",
                    "Perbaikan nutrisi dengan pakan berkualitas",
                    "Pemberian vitamin dan mineral tambahan",
                    "Isolasi sementara hewan yang terinfeksi"
                ),
                prevention = listOf(
                    "Deworming rutin setiap 3-6 bulan",
                    "Menjaga kebersihan kandang dan lingkungan",
                    "Pemberian pakan dan air bersih",
                    "Rotasi padang penggembalaan",
                    "Pemeriksaan feses berkala",
                    "Karantina hewan baru sebelum digabung"
                ),
                severity = "Sedang",
                imageRes = R.drawable.disease_cacingan,
                isContagious = true,
                affectedAnimals = listOf("Sapi", "Kerbau", "Kambing", "Domba")
            ),

            Disease(
                id = "pmk",
                name = "Penyakit Mulut dan Kuku (PMK)",
                scientificName = "Foot and Mouth Disease (FMD)",
                description = "Penyakit virus akut yang sangat menular pada hewan berkuku belah, ditandai dengan lepuh dan luka pada mulut, lidah, dan kuku.",
                symptoms = listOf(
                    "Lepuh berisi cairan pada lidah, gusi, dan hidung",
                    "Luka pada kuku dan sela-sela kuku",
                    "Hewan pincang dan sulit berjalan",
                    "Air liur berlebihan (hipersalivasi)",
                    "Demam tinggi (40-41°C)",
                    "Nafsu makan hilang",
                    "Penurunan produksi susu drastis",
                    "Kelemahan dan depresi"
                ),
                causes = listOf(
                    "Infeksi virus Foot and Mouth Disease Virus (FMDV)",
                    "Kontak langsung dengan hewan terinfeksi",
                    "Kontaminasi pakan, air, atau peralatan",
                    "Penyebaran melalui udara (aerosol)",
                    "Kendaraan dan manusia sebagai pembawa virus"
                ),
                treatment = listOf(
                    "Tidak ada pengobatan spesifik untuk virus PMK",
                    "Perawatan suportif untuk mencegah infeksi sekunder",
                    "Pemberian antibiotik untuk mencegah infeksi bakteri",
                    "Perawatan luka dengan antiseptik",
                    "Pemberian cairan dan elektrolit",
                    "Isolasi ketat hewan terinfeksi",
                    "Pelaporan wajib ke Dinas Peternakan"
                ),
                prevention = listOf(
                    "Vaksinasi rutin sesuai program pemerintah",
                    "Biosekuriti ketat di peternakan",
                    "Karantina hewan baru minimal 21 hari",
                    "Desinfeksi kendaraan dan peralatan",
                    "Pembatasan lalu lintas ternak",
                    "Monitoring kesehatan hewan rutin",
                    "Pelaporan kasus mencurigakan segera"
                ),
                severity = "Berat",
                imageRes = R.drawable.disease_pmk,
                isContagious = true,
                affectedAnimals = listOf("Sapi", "Kerbau", "Babi", "Kambing", "Domba")
            ),

            Disease(
                id = "lsd",
                name = "Lumpy Skin Disease (LSD)",
                scientificName = "Lumpy Skin Disease",
                description = "Penyakit virus yang menyerang sapi dan kerbau, ditandai dengan benjolan-benjolan pada kulit yang dapat menyebabkan kerugian ekonomi signifikan.",
                symptoms = listOf(
                    "Benjolan keras (nodul) berdiameter 2-5 cm pada kulit",
                    "Demam tinggi hingga 41°C",
                    "Pembengkakan kelenjar getah bening",
                    "Nafsu makan menurun drastis",
                    "Penurunan produksi susu hingga 50%",
                    "Kerusakan kulit dan kemungkinan infeksi sekunder",
                    "Edema pada kaki, skrotum, atau ambing",
                    "Discharge dari mata dan hidung"
                ),
                causes = listOf(
                    "Infeksi Lumpy Skin Disease Virus (LSDV)",
                    "Penularan melalui vektor serangga (lalat, nyamuk, kutu)",
                    "Kontak langsung dengan hewan terinfeksi",
                    "Kontaminasi melalui peralatan dan pakan",
                    "Kondisi lingkungan yang mendukung perkembangan vektor"
                ),
                treatment = listOf(
                    "Tidak ada pengobatan spesifik untuk virus LSD",
                    "Perawatan suportif dan simptomatik",
                    "Antibiotik untuk mencegah infeksi sekunder",
                    "Anti-inflamasi untuk mengurangi peradangan",
                    "Perawatan luka dan benjolan dengan antiseptik",
                    "Pemberian vitamin dan mineral untuk meningkatkan imunitas",
                    "Isolasi hewan terinfeksi"
                ),
                prevention = listOf(
                    "Vaksinasi dengan vaksin LSD yang tersedia",
                    "Pengendalian vektor serangga secara intensif",
                    "Karantina hewan baru minimal 28 hari",
                    "Biosekuriti ketat di peternakan",
                    "Monitoring kesehatan hewan rutin",
                    "Desinfeksi kandang dan peralatan",
                    "Pelaporan kasus mencurigakan ke otoritas"
                ),
                severity = "Berat",
                imageRes = R.drawable.disease_lsd,
                isContagious = true,
                affectedAnimals = listOf("Sapi", "Kerbau")
            )
        )

        filteredDiseases = allDiseases
    }


    private fun setupRecyclerView() {
        diseaseAdapter = DiseaseAdapter(filteredDiseases) { disease ->
            val intent = Intent(requireContext(), DiseaseDetailActivity::class.java)
            intent.putExtra("disease_id", disease.id)
            startActivity(intent)
        }

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
                filteredDiseases = if (query.isEmpty()) {
                    allDiseases
                } else {
                    allDiseases.filter { disease ->
                        disease.name.lowercase().contains(query) ||
                                disease.symptoms.any { it.lowercase().contains(query) } ||
                                disease.description.lowercase().contains(query)
                    }
                }
                diseaseAdapter.updateData(filteredDiseases)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
