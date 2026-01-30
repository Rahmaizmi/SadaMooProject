package com.example.sadamoo.users.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.databinding.DialogAdvancedFilterBinding
import java.text.SimpleDateFormat
import java.util.*

class AdvancedFilterDialog(
    private val onFilterApplied: (FilterCriteria) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogAdvancedFilterBinding
    private var startDate: Date? = null
    private var endDate: Date? = null

    data class FilterCriteria(
        val dateRange: Pair<Date?, Date?>,
        val severity: String,
        val cattleType: String,
        val diseaseType: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAdvancedFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        setupDatePickers()
        setupButtons()
    }

    private fun setupSpinners() {
        // Severity levels
        val severityLevels = arrayOf("Semua Tingkat", "Tinggi", "Sedang", "Rendah")
        val severityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, severityLevels)
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSeverity.adapter = severityAdapter

        // Cattle types
        val cattleTypes = arrayOf("Semua Jenis", "Sapi Madura", "Sapi Bali", "Sapi Limousin", "Sapi Simental")
        val cattleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cattleTypes)
        cattleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCattleType.adapter = cattleAdapter

        // Disease types
        val diseaseTypes = arrayOf("Semua Penyakit", "Sehat", "LSD", "PMK", "Cacingan")
        val diseaseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, diseaseTypes)
        diseaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDiseaseType.adapter = diseaseAdapter
    }

    private fun setupDatePickers() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        binding.btnStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    startDate = calendar.time
                    binding.btnStartDate.text = dateFormat.format(startDate!!)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    endDate = calendar.time
                    binding.btnEndDate.text = dateFormat.format(endDate!!)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButtons() {
        binding.btnApplyFilter.setOnClickListener {
            val criteria = FilterCriteria(
                dateRange = Pair(startDate, endDate),
                severity = binding.spinnerSeverity.selectedItem.toString(),
                cattleType = binding.spinnerCattleType.selectedItem.toString(),
                diseaseType = binding.spinnerDiseaseType.selectedItem.toString()
            )
            onFilterApplied(criteria)
            dismiss()
        }

        binding.btnResetFilter.setOnClickListener {
            // Reset all filters
            binding.spinnerSeverity.setSelection(0)
            binding.spinnerCattleType.setSelection(0)
            binding.spinnerDiseaseType.setSelection(0)
            startDate = null
            endDate = null
            binding.btnStartDate.text = "Pilih Tanggal Mulai"
            binding.btnEndDate.text = "Pilih Tanggal Akhir"
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
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

