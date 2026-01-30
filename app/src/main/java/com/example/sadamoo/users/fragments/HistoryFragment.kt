package com.example.sadamoo.users.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.R
import com.example.sadamoo.databinding.FragmentHistoryBinding
import com.example.sadamoo.users.ChatConsultationActivity
import com.example.sadamoo.users.ScanResultActivity
import com.example.sadamoo.users.adapters.HistoryAdapter
import com.example.sadamoo.users.data.Consultation
import com.example.sadamoo.users.data.Detection
import com.example.sadamoo.users.data.DetectionRoomDatabase
import com.example.sadamoo.users.dialogs.AdvancedFilterDialog
import com.example.sadamoo.utils.ConsultationSyncHelper
import kotlinx.coroutines.launch
import java.util.Date

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryAdapter

    private var allDetections = listOf<Detection>()
    private var allConsultations = listOf<Consultation>()
    private var allHistory = listOf<Any>()
    private var filteredHistory = listOf<Any>()
    private var currentFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Sync consultation dari Firestore
        syncConsultations()

        loadHistoryFromDb()
        setupRecyclerView()
        setupSearch()
        setupFilters()
    }

    private fun syncConsultations() {
        lifecycleScope.launch {
            try {
                val syncHelper = ConsultationSyncHelper(requireContext())
                syncHelper.syncConsultationsFromFirestore()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadHistoryFromDb() {
        val detectionDao = DetectionRoomDatabase.getDatabase(requireContext()).detectionDao()
        val consultationDao = DetectionRoomDatabase.getDatabase(requireContext()).consultationDao()

        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            // Jika user belum login
            updateEmptyState()
            return
        }

        detectionDao.getDetectionsByUserId(userId)
            .observe(viewLifecycleOwner) { detections ->
                allDetections = detections
                combineAndFilterHistory()
            }

        userId.let { id ->
            consultationDao.getConsultationsByUserId(id)
                .observe(viewLifecycleOwner) { consultations ->
                    allConsultations = consultations
                    combineAndFilterHistory()
                }
        }

    }

    private fun combineAndFilterHistory() {
        // Gabungkan detection dan consultation
        val combined = mutableListOf<Any>()
        combined.addAll(allDetections)
        combined.addAll(allConsultations)

        // Sort by timestamp descending
        allHistory = combined.sortedByDescending { item ->
            when (item) {
                is Detection -> item.timestamp
                is Consultation -> item.timestamp
                else -> Date(0)
            }
        }

        filterHistory(binding.etSearch.text.toString(), currentFilter)
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            filteredHistory,
            onItemClick = { item ->
                when (item) {
                    is Detection -> {
                        // Handle Detection click
                        val resultType = when {
                            item.cattleType == "Tidak Dapat Mendeteksi" -> "undetected"
                            item.isHealthy && item.detectedDisease == null -> "cattle_type"
                            else -> "disease"
                        }

                        val displayName = when (resultType) {
                            "undetected" -> "Tidak Dapat Mendeteksi"
                            "cattle_type" -> item.cattleType
                            "disease" -> item.detectedDisease ?: "Penyakit Tidak Dikenal"
                            else -> item.cattleType
                        }

                        val intent =
                            Intent(requireContext(), ScanResultActivity::class.java).apply {
                                putExtra("imagePath", item.imagePath)
                                putExtra("resultType", resultType)
                                putExtra("displayName", displayName)
                                putExtra("confidence", item.confidence)
                                putExtra("isHealthy", item.isHealthy)
                                putExtra("detectedDisease", item.detectedDisease)
                                putExtra("is_from_history", true)
                            }
                        startActivity(intent)
                    }

                    is Consultation -> {
                        // Handle Consultation click - buka chat
                        val intent =
                            Intent(requireContext(), ChatConsultationActivity::class.java).apply {
                                putExtra("doctor_id", item.doctorId)
                                putExtra("chat_room_id", item.id)
                                putExtra("doctor_name", item.doctorName)
                                putExtra("timestamp", item.timestamp.time)
                                putExtra("is_from_history", true)
                            }
                        startActivity(intent)
                    }
                }
            },
            onDeleteClick = { item ->
                lifecycleScope.launch {
                    when (item) {
                        is Detection -> {
                            val dao =
                                DetectionRoomDatabase.getDatabase(requireContext()).detectionDao()
                            dao.delete(item)
                        }

                        is Consultation -> {
                            val dao = DetectionRoomDatabase.getDatabase(requireContext())
                                .consultationDao()
                            dao.delete(item)
                        }
                    }
                }
            }
        )

        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                filterHistory(query, currentFilter)
            }
        })

        binding.ivFilter.setOnClickListener {
            val dialog = AdvancedFilterDialog { criteria ->
                applyAdvancedFilter(criteria)
            }
            dialog.show(parentFragmentManager, "AdvancedFilter")
        }
    }

    private fun setupFilters() {
        binding.btnFilterAll.setOnClickListener {
            setActiveFilter("all")
            filterHistory(binding.etSearch.text.toString(), "all")
        }

        binding.btnFilterScan.setOnClickListener {
            setActiveFilter("scan")
            filterHistory(binding.etSearch.text.toString(), "scan")
        }

        binding.btnFilterConsultation.setOnClickListener {
            setActiveFilter("consultation")
            filterHistory(binding.etSearch.text.toString(), "consultation")
        }
    }

    private fun setActiveFilter(filter: String) {
        currentFilter = filter

        // Reset all buttons
        binding.btnFilterAll.background =
            requireContext().getDrawable(R.drawable.filter_button_inactive)
        binding.btnFilterAll.setTextColor(Color.parseColor("#4A90E2"))
        binding.btnFilterScan.background =
            requireContext().getDrawable(R.drawable.filter_button_inactive)
        binding.btnFilterScan.setTextColor(Color.parseColor("#4A90E2"))
        binding.btnFilterConsultation.background =
            requireContext().getDrawable(R.drawable.filter_button_inactive)
        binding.btnFilterConsultation.setTextColor(Color.parseColor("#4A90E2"))

        // Set active button
        when (filter) {
            "all" -> {
                binding.btnFilterAll.background =
                    requireContext().getDrawable(R.drawable.filter_button_active)
                binding.btnFilterAll.setTextColor(Color.WHITE)
            }

            "scan" -> {
                binding.btnFilterScan.background =
                    requireContext().getDrawable(R.drawable.filter_button_active)
                binding.btnFilterScan.setTextColor(Color.WHITE)
            }

            "consultation" -> {
                binding.btnFilterConsultation.background =
                    requireContext().getDrawable(R.drawable.filter_button_active)
                binding.btnFilterConsultation.setTextColor(Color.WHITE)
            }
        }
    }

    private fun filterHistory(query: String, filter: String) {
        var filtered = allHistory

        // Filter by type
        when (filter) {
            "scan" -> {
                filtered = allHistory.filterIsInstance<Detection>()
            }

            "consultation" -> {
                filtered = allHistory.filterIsInstance<Consultation>()
            }

            "all" -> {
                // Show everything
                filtered = allHistory
            }
        }

        // Filter by search query
        if (query.isNotEmpty()) {
            filtered = filtered.filter { item ->
                when (item) {
                    is Detection -> {
                        item.cattleType.lowercase().contains(query) ||
                                item.detectedDisease?.lowercase()?.contains(query) == true ||
                                item.description.lowercase().contains(query)
                    }

                    is Consultation -> {
                        item.doctorName.lowercase().contains(query) ||
                                item.lastMessage.lowercase().contains(query)
                    }

                    else -> false
                }
            }
        }

        filteredHistory = filtered
        historyAdapter.updateData(filteredHistory)
        updateEmptyState()
    }

    private fun applyAdvancedFilter(criteria: AdvancedFilterDialog.FilterCriteria) {
        var filtered = allHistory

        // Date range filter
        if (criteria.dateRange.first != null && criteria.dateRange.second != null) {
            filtered = filtered.filter { item ->
                val itemDate = when (item) {
                    is Detection -> item.timestamp
                    is Consultation -> item.timestamp
                    else -> Date(0)
                }
                val startDate = criteria.dateRange.first!!
                val endDate = criteria.dateRange.second!!
                itemDate.after(startDate) && itemDate.before(endDate)
            }
        }

        // Confidence/severity filter (hanya untuk Detection)
        if (criteria.severity != "Semua Tingkat") {
            filtered = filtered.filter { item ->
                if (item is Detection) {
                    when (criteria.severity) {
                        "Tinggi" -> item.confidence > 0.8f
                        "Sedang" -> item.confidence in 0.5f..0.8f
                        "Rendah" -> item.confidence < 0.5f
                        else -> true
                    }
                } else {
                    true // Consultation tidak di-filter by confidence
                }
            }
        }

        // Cattle type filter (hanya untuk Detection)
        if (criteria.cattleType != "Semua Jenis") {
            filtered = filtered.filter { item ->
                if (item is Detection) {
                    item.cattleType == criteria.cattleType
                } else {
                    true // Consultation tidak di-filter by cattle type
                }
            }
        }

        // Disease filter (hanya untuk Detection)
        if (criteria.diseaseType != "Semua Penyakit") {
            filtered = filtered.filter { item ->
                if (item is Detection) {
                    item.detectedDisease == criteria.diseaseType ||
                            (criteria.diseaseType == "Sehat" && item.isHealthy)
                } else {
                    true // Consultation tidak di-filter by disease
                }
            }
        }

        filteredHistory = filtered
        historyAdapter.updateData(filteredHistory)
        updateEmptyState()

        android.widget.Toast.makeText(
            requireContext(),
            "Filter diterapkan: ${filtered.size} hasil",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateEmptyState() {
        if (filteredHistory.isEmpty()) {
            binding.rvHistory.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvHistory.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        syncConsultations()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}