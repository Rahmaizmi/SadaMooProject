package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.admin.models.PackageModel
import com.example.sadamoo.databinding.DialogAddEditPackageBinding
import java.util.*

class AddEditPackageDialog : DialogFragment() {
    private lateinit var binding: DialogAddEditPackageBinding
    private var packageModel: PackageModel? = null
    private lateinit var onPackageSaved: (PackageModel) -> Unit

    private var selectedDurationType = "month"
    private val featuresList = mutableListOf<String>()

    companion object {
        fun newInstance(
            packageModel: PackageModel?,
            onPackageSaved: (PackageModel) -> Unit
        ): AddEditPackageDialog {
            val dialog = AddEditPackageDialog()
            val args = Bundle()
            if (packageModel != null) {
                args.putParcelable("package", packageModel)
            }
            dialog.arguments = args
            dialog.onPackageSaved = onPackageSaved
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageModel = arguments?.getParcelable("package")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAddEditPackageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupDurationTypeButtons()
        setupFeatureManagement()
        setupButtons()

        if (packageModel != null) {
            loadPackageData()
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
        val title = if (packageModel != null) "Edit Package" else "Add New Package"
        binding.tvDialogTitle.text = title
    }

    private fun setupDurationTypeButtons() {
        setDurationType("month") // Default

        binding.btnMonth.setOnClickListener { setDurationType("month") }
        binding.btnYear.setOnClickListener { setDurationType("year") }
    }

    private fun setDurationType(type: String) {
        selectedDurationType = type

        // Reset button styles
        binding.btnMonth.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_type_inactive)
        binding.btnYear.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_type_inactive)

        // Set active button
        when (type) {
            "month" -> binding.btnMonth.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_type_active)
            "year" -> binding.btnYear.background = requireContext().getDrawable(com.example.sadamoo.R.drawable.duration_type_active)
        }

        // Update duration hint
        val hint = if (type == "month") "Duration in months (e.g., 1, 3, 6)" else "Duration in years (e.g., 1, 2)"
        binding.etDuration.hint = hint
    }

    private fun setupFeatureManagement() {
        binding.btnAddFeature.setOnClickListener {
            val feature = binding.etNewFeature.text.toString().trim()
            if (feature.isNotEmpty()) {
                featuresList.add(feature)
                binding.etNewFeature.text.clear()
                updateFeaturesDisplay()
            } else {
                Toast.makeText(requireContext(), "Please enter a feature", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFeaturesDisplay() {
        val featuresText = featuresList.mapIndexed { index, feature ->
            "${index + 1}. $feature"
        }.joinToString("\n")

        binding.tvFeaturesList.text = if (featuresText.isNotEmpty()) {
            featuresText
        } else {
            "No features added yet"
        }

        binding.tvFeatureCount.text = "${featuresList.size} features"

        // Show clear button if there are features
        binding.btnClearFeatures.visibility = if (featuresList.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadPackageData() {
        packageModel?.let { pkg ->
            binding.apply {
                etPackageName.setText(pkg.name)
                etPackageDescription.setText(pkg.description)
                etPrice.setText(pkg.price.toString())
                etDuration.setText(pkg.duration.toString())

                setDurationType(pkg.durationType)

                featuresList.clear()
                featuresList.addAll(pkg.features)
                updateFeaturesDisplay()

                switchIsActive.isChecked = pkg.isActive
            }
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            savePackage()
        }

        binding.btnClearFeatures.setOnClickListener {
            featuresList.clear()
            updateFeaturesDisplay()
        }
    }

    private fun savePackage() {
        val name = binding.etPackageName.text.toString().trim()
        val description = binding.etPackageDescription.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        val durationText = binding.etDuration.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            binding.etPackageName.error = "Package name is required"
            return
        }

        if (description.isEmpty()) {
            binding.etPackageDescription.error = "Description is required"
            return
        }

        val price = priceText.toIntOrNull()
        if (price == null || price <= 0) {
            binding.etPrice.error = "Valid price is required"
            return
        }

        val duration = durationText.toIntOrNull()
        if (duration == null || duration <= 0) {
            binding.etDuration.error = "Valid duration is required"
            return
        }

        if (featuresList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one feature", Toast.LENGTH_SHORT).show()
            return
        }

        // Create or update package
        val newPackage = PackageModel(
            id = packageModel?.id ?: "",
            name = name,
            description = description,
            price = price,
            duration = duration,
            durationType = selectedDurationType,
            features = featuresList.toList(),
            isActive = binding.switchIsActive.isChecked,
            createdAt = packageModel?.createdAt ?: Date(),
            updatedAt = Date(),
            subscriberCount = packageModel?.subscriberCount ?: 0
        )

        onPackageSaved(newPackage)
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
