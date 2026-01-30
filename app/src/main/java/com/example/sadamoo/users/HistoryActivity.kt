package com.example.sadamoo.users
//
//import android.content.Context
//import android.content.Intent
//import android.graphics.Color
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.sadamoo.R
//import com.example.sadamoo.databinding.ActivityHistoryBinding
//import com.example.sadamoo.users.adapters.HistoryAdapter
//import com.example.sadamoo.users.data.Consultation
//import com.example.sadamoo.users.data.Detection
//import com.example.sadamoo.users.data.DetectionRoomDatabase
//import com.example.sadamoo.users.dialogs.AdvancedFilterDialog
//import com.example.sadamoo.utils.ConsultationSyncHelper
//import com.example.sadamoo.utils.applyStatusBarPadding
//import kotlinx.coroutines.launch
//import java.util.Date
//
//class HistoryActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityHistoryBinding
//    private lateinit var historyAdapter: HistoryAdapter
//
//    private var allDetections = listOf<Detection>()
//    private var allConsultations = listOf<Consultation>()
//    private var allHistory = listOf<Any>() // Detection atau Consultation
//    private var filteredHistory = listOf<Any>()
//    private var currentFilter = "all"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityHistoryBinding.inflate(layoutInflater)
//        binding.root.applyStatusBarPadding()
//        setContentView(binding.root)
//
//        setupBottomNavigation()
//
//        // Sync consultation dari Firestore
//        syncConsultations()
//
//        loadHistoryFromDb()
//        setupRecyclerView()
//        setupSearch()
//        setupFilters()
//    }
//
//    private fun syncConsultations() {
//        lifecycleScope.launch {
//            try {
//                val syncHelper = ConsultationSyncHelper(this@HistoryActivity)
//                syncHelper.syncConsultationsFromFirestore()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun loadHistoryFromDb() {
//        val detectionDao = DetectionRoomDatabase.getDatabase(this).detectionDao()
//        val consultationDao = DetectionRoomDatabase.getDatabase(this).consultationDao()
//
//        // Load detections
////        detectionDao.getAllDetections().observe(this) { detections ->
////            allDetections = detections
////            combineAndFilterHistory()
////        }
//
//        // Load consultations
////        consultationDao.getConsultationsByUserId(userId!!).observe(this) { consultations ->
////            allConsultations = consultations
////            combineAndFilterHistory()
////        }
//
//        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
//        val userId = currentUser?.uid
//
//        if (userId == null) {
//            // Jika user belum login
//            updateEmptyState()
//            return
//        }
//
//        detectionDao.getDetectionsByUserId(userId)
//            .observe(this) { detections ->
//                allDetections = detections
//                combineAndFilterHistory()
//            }
//
//        userId.let { id ->
//            consultationDao.getConsultationsByUserId(id)
//                .observe(this) { consultations ->
//                    allConsultations = consultations
//                    combineAndFilterHistory()
//                }
//        }
//    }
//
//    private fun combineAndFilterHistory() {
//        // Gabungkan detection dan consultation
//        val combined = mutableListOf<Any>()
//        combined.addAll(allDetections)
//        combined.addAll(allConsultations)
//
//        // Sort by timestamp descending
//        allHistory = combined.sortedByDescending { item ->
//            when (item) {
//                is Detection -> item.timestamp
//                is Consultation -> item.timestamp
//                else -> Date(0)
//            }
//        }
//        filterHistory(binding.etSearch.text.toString(), currentFilter)
//    }
//
//    private fun setupRecyclerView() {
//        historyAdapter = HistoryAdapter(
//            filteredHistory,
//            onItemClick = { item ->
//                when (item) {
//                    is Detection -> {
//                        // Handle Detection click
//                        val resultType = when {
//                            item.cattleType == "Tidak Dapat Mendeteksi" -> "undetected"
//                            item.isHealthy && item.detectedDisease == null -> "cattle_type"
//                            else -> "disease"
//                        }
//
//                        val displayName = when (resultType) {
//                            "undetected" -> "Tidak Dapat Mendeteksi"
//                            "cattle_type" -> item.cattleType
//                            "disease" -> item.detectedDisease ?: "Penyakit Tidak Dikenal"
//                            else -> item.cattleType
//                        }
//
//                        val intent = Intent(this, ScanResultActivity::class.java).apply {
//                            putExtra("imagePath", item.imagePath)
//                            putExtra("resultType", resultType)
//                            putExtra("displayName", displayName)
//                            putExtra("confidence", item.confidence)
//                            putExtra("isHealthy", item.isHealthy)
//                            putExtra("detectedDisease", item.detectedDisease)
//                            putExtra("is_from_history", true)
//                        }
//                        startActivity(intent)
//                    }
//
//                    is Consultation -> {
//                        // Handle Consultation click - buka chat
//                        val intent = Intent(this, ChatConsultationActivity::class.java).apply {
//                            putExtra("doctor_name", item.doctorName)
//                            putExtra("doctor_id", item.doctorId)
//                            putExtra("chat_room_id", item.id)
//                            putExtra("is_from_history", true)
//                        }
//                        startActivity(intent)
//                    }
//                }
//            },
//            onDeleteClick = { item ->
//                lifecycleScope.launch {
//                    when (item) {
//                        is Detection -> {
//                            val dao = DetectionRoomDatabase.getDatabase(this@HistoryActivity)
//                                .detectionDao()
//                            dao.delete(item)
//                        }
//
//                        is Consultation -> {
//                            val dao = DetectionRoomDatabase.getDatabase(this@HistoryActivity)
//                                .consultationDao()
//                            dao.delete(item)
//                        }
//                    }
//                }
//            }
//        )
//
//        binding.rvHistory.apply {
//            layoutManager = LinearLayoutManager(this@HistoryActivity)
//            adapter = historyAdapter
//        }
//    }
//
//    private fun setupSearch() {
//        binding.etSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                val query = s.toString().lowercase().trim()
//                filterHistory(query, currentFilter)
//            }
//        })
//
//        binding.ivFilter.setOnClickListener {
//            val dialog = AdvancedFilterDialog { criteria ->
//                applyAdvancedFilter(criteria)
//            }
//            dialog.show(supportFragmentManager, "AdvancedFilter")
//        }
//    }
//
//    private fun applyAdvancedFilter(criteria: AdvancedFilterDialog.FilterCriteria) {
//        var filtered = allHistory
//
//        // Date range filter
//        if (criteria.dateRange.first != null && criteria.dateRange.second != null) {
//            filtered = filtered.filter { item ->
//                val itemDate = when (item) {
//                    is Detection -> item.timestamp
//                    is Consultation -> item.timestamp
//                    else -> Date(0)
//                }
//                val startDate = criteria.dateRange.first!!
//                val endDate = criteria.dateRange.second!!
//                itemDate.after(startDate) && itemDate.before(endDate)
//            }
//        }
//
//        // Confidence/severity filter (hanya untuk Detection)
//        if (criteria.severity != "Semua Tingkat") {
//            filtered = filtered.filter { item ->
//                if (item is Detection) {
//                    when (criteria.severity) {
//                        "Tinggi" -> item.confidence > 0.8f
//                        "Sedang" -> item.confidence in 0.5f..0.8f
//                        "Rendah" -> item.confidence < 0.5f
//                        else -> true
//                    }
//                } else {
//                    true // Consultation tidak di-filter by confidence
//                }
//            }
//        }
//
//        // Cattle type filter (hanya untuk Detection)
//        if (criteria.cattleType != "Semua Jenis") {
//            filtered = filtered.filter { item ->
//                if (item is Detection) {
//                    item.cattleType == criteria.cattleType
//                } else {
//                    true // Consultation tidak di-filter by cattle type
//                }
//            }
//        }
//
//        // Disease filter (hanya untuk Detection)
//        if (criteria.diseaseType != "Semua Penyakit") {
//            filtered = filtered.filter { item ->
//                if (item is Detection) {
//                    item.detectedDisease == criteria.diseaseType ||
//                            (criteria.diseaseType == "Sehat" && item.isHealthy)
//                } else {
//                    true // Consultation tidak di-filter by disease
//                }
//            }
//        }
//
//        filteredHistory = filtered
//        historyAdapter.updateData(filteredHistory)
//        updateEmptyState()
//
//        android.widget.Toast.makeText(
//            this,
//            "Filter diterapkan: ${filtered.size} hasil",
//            android.widget.Toast.LENGTH_SHORT
//        ).show()
//    }
//
//    private fun setupFilters() {
//        binding.btnFilterAll.setOnClickListener {
//            setActiveFilter("all")
//            filterHistory(binding.etSearch.text.toString(), "all")
//        }
//
//        binding.btnFilterScan.setOnClickListener {
//            setActiveFilter("scan")
//            filterHistory(binding.etSearch.text.toString(), "scan")
//        }
//
//        binding.btnFilterConsultation.setOnClickListener {
//            setActiveFilter("consultation")
//            filterHistory(binding.etSearch.text.toString(), "consultation")
//        }
//    }
//
//    private fun setActiveFilter(filter: String) {
//        currentFilter = filter
//
//        // Reset all buttons
//        binding.btnFilterAll.background = getDrawable(R.drawable.filter_button_inactive)
//        binding.btnFilterAll.setTextColor(Color.parseColor("#4A90E2"))
//        binding.btnFilterScan.background = getDrawable(R.drawable.filter_button_inactive)
//        binding.btnFilterScan.setTextColor(Color.parseColor("#4A90E2"))
//        binding.btnFilterConsultation.background = getDrawable(R.drawable.filter_button_inactive)
//        binding.btnFilterConsultation.setTextColor(Color.parseColor("#4A90E2"))
//
//        // Set active button
//        when (filter) {
//            "all" -> {
//                binding.btnFilterAll.background = getDrawable(R.drawable.filter_button_active)
//                binding.btnFilterAll.setTextColor(Color.WHITE)
//            }
//
//            "scan" -> {
//                binding.btnFilterScan.background = getDrawable(R.drawable.filter_button_active)
//                binding.btnFilterScan.setTextColor(Color.WHITE)
//            }
//
//            "consultation" -> {
//                binding.btnFilterConsultation.background =
//                    getDrawable(R.drawable.filter_button_active)
//                binding.btnFilterConsultation.setTextColor(Color.WHITE)
//            }
//        }
//    }
//
//    private fun filterHistory(query: String, filter: String) {
//        var filtered = allHistory
//
//        // Filter by type
//        when (filter) {
//            "scan" -> {
//                filtered = allHistory.filterIsInstance<Detection>()
//            }
//
//            "consultation" -> {
//                filtered = allHistory.filterIsInstance<Consultation>()
//            }
//
//            "all" -> {
//                // Show everything
//                filtered = allHistory
//            }
//        }
//
//        // Filter by search query
//        if (query.isNotEmpty()) {
//            filtered = filtered.filter { item ->
//                when (item) {
//                    is Detection -> {
//                        item.cattleType.lowercase().contains(query) ||
//                                item.detectedDisease?.lowercase()?.contains(query) == true ||
//                                item.description.lowercase().contains(query)
//                    }
//
//                    is Consultation -> {
//                        item.doctorName.lowercase().contains(query) ||
//                                item.lastMessage.lowercase().contains(query)
//                    }
//
//                    else -> false
//                }
//            }
//        }
//
//        filteredHistory = filtered
//        historyAdapter.updateData(filteredHistory)
//        updateEmptyState()
//    }
//
//    private fun updateEmptyState() {
//        if (filteredHistory.isEmpty()) {
//            binding.rvHistory.visibility = android.view.View.GONE
//            binding.layoutEmptyState.visibility = android.view.View.VISIBLE
//        } else {
//            binding.rvHistory.visibility = android.view.View.VISIBLE
//            binding.layoutEmptyState.visibility = android.view.View.GONE
//        }
//    }
//
//    private fun setupBottomNavigation() {
//        setActiveNav(binding.navRiwayat)
//
//        binding.navBeranda.setOnClickListener {
//            finish()
//        }
//
//        binding.navInformasi.setOnClickListener {
//            startActivity(Intent(this, InformationActivity::class.java))
//        }
//
//        binding.fabDeteksi.setOnClickListener {
//            startActivity(Intent(this, CameraScanActivity::class.java))
//        }
//
//        binding.navRiwayat.setOnClickListener {
//            setActiveNav(binding.navRiwayat)
//        }
//
//        binding.navProfil.setOnClickListener {
//            // startActivity(Intent(this, ProfileActivity::class.java))
//        }
//    }
//
//    private fun setActiveNav(activeNav: LinearLayout) {
//        val allNavs =
//            listOf(binding.navBeranda, binding.navInformasi, binding.navRiwayat, binding.navProfil)
//        val activeColor = Color.parseColor("#4A90E2")
//        val inactiveColor = Color.parseColor("#B0B0B0")
//
//        for (nav in allNavs) {
//            val icon = nav.getChildAt(0) as ImageView
//            val label = nav.getChildAt(1) as TextView
//
//            if (nav == activeNav) {
//                icon.setColorFilter(activeColor)
//                label.setTextColor(activeColor)
//            } else {
//                icon.setColorFilter(inactiveColor)
//                label.setTextColor(inactiveColor)
//            }
//        }
//    }
//}