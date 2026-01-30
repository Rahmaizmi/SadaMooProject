package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.admin.models.DiseaseModel
import com.example.sadamoo.databinding.DialogAddEditDiseaseBinding
import java.util.*

class AddEditDiseaseDialog : DialogFragment() {
    private lateinit var binding: DialogAddEditDiseaseBinding
    private var diseaseModel: DiseaseModel? = null
    private lateinit var onDiseaseSaved: (DiseaseModel) -> Unit

    private var selectedSeverity = "moderate"
    private val symptomsList = mutableListOf<String>()
    private val causesList = mutableListOf<String>()
    private val treatmentsList = mutableListOf<String>()
    private val preventionList = mutableListOf<String>()

    companion object {
        fun newInstance(
            diseaseModel: DiseaseModel?,
            onDiseaseSaved: (DiseaseModel) -> Unit
        ): AddEditDiseaseDialog {
            val dialog = AddEditDiseaseDialog()
            val args = Bundle()
            if (diseaseModel != null) {
                args.putParcelable("disease", diseaseModel)
            }
            dialog.arguments = args
            dialog.onDiseaseSaved = onDiseaseSaved
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diseaseModel = arguments?.getParcelable("disease")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAddEditDiseaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupSeverityButtons()
        setupListManagement()
        setupButtons()

        if (diseaseModel != null) {
            loadDiseaseData()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }


    private fun setupUI() {
        val title = if (diseaseModel != null) "Edit Disease" else "Add New Disease"
        binding.tvDialogTitle.text = title
    }

    private fun setupSeverityButtons() {
        setSeverity("moderate") // Default

        binding.btnMild.setOnClickListener { setSeverity("mild") }
        binding.btnModerate.setOnClickListener { setSeverity("moderate") }
        binding.btnSevere.setOnClickListener { setSeverity("severe") }
    }

    private fun setSeverity(severity: String) {
        selectedSeverity = severity

        // Reset button styles
        binding.btnMild.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.severity_button_inactive)
        binding.btnModerate.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.severity_button_inactive)
        binding.btnSevere.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.severity_button_inactive)

        // Set active button
        when (severity) {
            "mild" -> binding.btnMild.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.severity_button_mild)
            "moderate" -> binding.btnModerate.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.severity_button_moderate)
            "severe" -> binding.btnSevere.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.severity_button_severe)
        }
    }

    private fun setupListManagement() {
        // Symptoms
        binding.btnAddSymptom.setOnClickListener {
            val symptom = binding.etNewSymptom.text.toString().trim()
            if (symptom.isNotEmpty()) {
                symptomsList.add(symptom)
                binding.etNewSymptom.text.clear()
                updateSymptomsDisplay()
            }
        }

        // Causes
        binding.btnAddCause.setOnClickListener {
            val cause = binding.etNewCause.text.toString().trim()
            if (cause.isNotEmpty()) {
                causesList.add(cause)
                binding.etNewCause.text.clear()
                updateCausesDisplay()
            }
        }

        // Treatments
        binding.btnAddTreatment.setOnClickListener {
            val treatment = binding.etNewTreatment.text.toString().trim()
            if (treatment.isNotEmpty()) {
                treatmentsList.add(treatment)
                binding.etNewTreatment.text.clear()
                updateTreatmentsDisplay()
            }
        }

        // Prevention
        binding.btnAddPrevention.setOnClickListener {
            val prevention = binding.etNewPrevention.text.toString().trim()
            if (prevention.isNotEmpty()) {
                preventionList.add(prevention)
                binding.etNewPrevention.text.clear()
                updatePreventionDisplay()
            }
        }
    }

    private fun updateSymptomsDisplay() {
        val text = symptomsList.mapIndexed { index, item -> "${index + 1}. $item" }.joinToString("\n")
        binding.tvSymptomsList.text = if (text.isNotEmpty()) text else "No symptoms added"
        binding.tvSymptomsCount.text = "${symptomsList.size} symptoms"
    }

    private fun updateCausesDisplay() {
        val text = causesList.mapIndexed { index, item -> "${index + 1}. $item" }.joinToString("\n")
        binding.tvCausesList.text = if (text.isNotEmpty()) text else "No causes added"
        binding.tvCausesCount.text = "${causesList.size} causes"
    }

    private fun updateTreatmentsDisplay() {
        val text = treatmentsList.mapIndexed { index, item -> "${index + 1}. $item" }.joinToString("\n")
        binding.tvTreatmentsList.text = if (text.isNotEmpty()) text else "No treatments added"
        binding.tvTreatmentsCount.text = "${treatmentsList.size} treatments"
    }

    private fun updatePreventionDisplay() {
        val text = preventionList.mapIndexed { index, item -> "${index + 1}. $item" }.joinToString("\n")
        binding.tvPreventionList.text = if (text.isNotEmpty()) text else "No prevention methods added"
        binding.tvPreventionCount.text = "${preventionList.size} methods"
    }

    private fun loadDiseaseData() {
        diseaseModel?.let { disease ->
            binding.apply {
                etDiseaseName.setText(disease.name)
                etScientificName.setText(disease.scientificName)
                etDiseaseDescription.setText(disease.description)
                etEstimatedLoss.setText(disease.estimatedLoss.toString())
                etRecoveryTime.setText(disease.recoveryTime)

                setSeverity(disease.severity)
                switchContagious.isChecked = disease.contagious
                switchIsActive.isChecked = disease.isActive

                // Load lists
                symptomsList.clear()
                symptomsList.addAll(disease.symptoms)
                updateSymptomsDisplay()

                causesList.clear()
                causesList.addAll(disease.causes)
                updateCausesDisplay()

                treatmentsList.clear()
                treatmentsList.addAll(disease.treatments)
                updateTreatmentsDisplay()

                preventionList.clear()
                preventionList.addAll(disease.prevention)
                updatePreventionDisplay()
            }
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveDisease()
        }

        // Clear buttons
        binding.btnClearSymptoms.setOnClickListener {
            symptomsList.clear()
            updateSymptomsDisplay()
        }

        binding.btnClearCauses.setOnClickListener {
            causesList.clear()
            updateCausesDisplay()
        }

        binding.btnClearTreatments.setOnClickListener {
            treatmentsList.clear()
            updateTreatmentsDisplay()
        }

        binding.btnClearPrevention.setOnClickListener {
            preventionList.clear()
            updatePreventionDisplay()
        }
    }

    private fun saveDisease() {
        val name = binding.etDiseaseName.text.toString().trim()
        val scientificName = binding.etScientificName.text.toString().trim()
        val description = binding.etDiseaseDescription.text.toString().trim()
        val estimatedLossText = binding.etEstimatedLoss.text.toString().trim()
        val recoveryTime = binding.etRecoveryTime.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            binding.etDiseaseName.error = "Disease name is required"
            return
        }

        if (scientificName.isEmpty()) {
            binding.etScientificName.error = "Scientific name is required"
            return
        }

        if (description.isEmpty()) {
            binding.etDiseaseDescription.error = "Description is required"
            return
        }

        val estimatedLoss = estimatedLossText.toIntOrNull()
        if (estimatedLoss == null || estimatedLoss < 0) {
            binding.etEstimatedLoss.error = "Valid estimated loss is required"
            return
        }

        if (recoveryTime.isEmpty()) {
            binding.etRecoveryTime.error = "Recovery time is required"
            return
        }

        if (symptomsList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one symptom", Toast.LENGTH_SHORT).show()
            return
        }

        if (causesList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one cause", Toast.LENGTH_SHORT).show()
            return
        }

        if (treatmentsList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one treatment", Toast.LENGTH_SHORT).show()
            return
        }

        if (preventionList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one prevention method", Toast.LENGTH_SHORT).show()
            return
        }

        // Create or update disease
        val newDisease = DiseaseModel(
            id = diseaseModel?.id ?: "",
            name = name,
            scientificName = scientificName,
            description = description,
            symptoms = symptomsList.toList(),
            causes = causesList.toList(),
            treatments = treatmentsList.toList(),
            prevention = preventionList.toList(),
            severity = selectedSeverity,
            isActive = binding.switchIsActive.isChecked,
            estimatedLoss = estimatedLoss,
            recoveryTime = recoveryTime,
            contagious = binding.switchContagious.isChecked,
            createdAt = diseaseModel?.createdAt ?: Date(),
            updatedAt = Date(),
            detectionCount = diseaseModel?.detectionCount ?: 0
        )

        onDiseaseSaved(newDisease)
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}

