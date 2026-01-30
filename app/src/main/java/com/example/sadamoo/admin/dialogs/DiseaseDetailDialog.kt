package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.admin.models.DiseaseModel
import com.example.sadamoo.databinding.DialogDiseaseDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class DiseaseDetailDialog : DialogFragment() {
    private lateinit var binding: DialogDiseaseDetailBinding
    private lateinit var disease: DiseaseModel

    companion object {
        fun newInstance(disease: DiseaseModel): DiseaseDetailDialog {
            val dialog = DiseaseDetailDialog()
            val args = Bundle()
            args.putParcelable("disease", disease)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disease = arguments?.getParcelable("disease") ?: return
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDiseaseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDiseaseDetails()

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupDiseaseDetails() {
        binding.apply {
            tvDiseaseName.text = disease.name
            tvScientificName.text = disease.scientificName
            tvDiseaseDescription.text = disease.description

            // Status
            tvDiseaseStatus.text = if (disease.isActive) "ACTIVE" else "INACTIVE"
            tvDiseaseStatus.setTextColor(
                if (disease.isActive)
                    requireContext().getColor(android.R.color.holo_green_dark)
                else
                    requireContext().getColor(android.R.color.holo_red_dark)
            )

            // Severity
            tvSeverity.text = disease.getSeverityText()
            tvSeverity.setTextColor(android.graphics.Color.parseColor(disease.getSeverityColor()))

            // Contagious
            tvContagious.text = disease.getContagiousText()
            tvContagious.setTextColor(
                if (disease.contagious)
                    requireContext().getColor(android.R.color.holo_red_dark)
                else
                    requireContext().getColor(android.R.color.holo_green_dark)
            )

            // Recovery time and estimated loss
            tvRecoveryTime.text = disease.recoveryTime
            tvEstimatedLoss.text = disease.getFormattedLoss()

            // Detection count
            tvDetectionCount.text = "${disease.detectionCount} times detected"

            // Lists
            tvSymptomsList.text = disease.symptoms.mapIndexed { index, symptom ->
                "✓ $symptom"
            }.joinToString("\n")

            tvCausesList.text = disease.causes.mapIndexed { index, cause ->
                "• $cause"
            }.joinToString("\n")

            tvTreatmentsList.text = disease.treatments.mapIndexed { index, treatment ->
                "⚕ $treatment"
            }.joinToString("\n")

            tvPreventionList.text = disease.prevention.mapIndexed { index, prevention ->
                "🛡 $prevention"
            }.joinToString("\n")

            // Dates
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            tvCreatedDate.text = "Created: ${dateFormat.format(disease.createdAt)}"
            tvUpdatedDate.text = "Last updated: ${dateFormat.format(disease.updatedAt)}"
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}


