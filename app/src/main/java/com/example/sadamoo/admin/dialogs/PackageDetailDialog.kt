package com.example.sadamoo.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.admin.models.PackageModel
import com.example.sadamoo.databinding.DialogPackageDetailBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PackageDetailDialog : DialogFragment() {
    private lateinit var binding: DialogPackageDetailBinding
    private lateinit var packageModel: PackageModel

    companion object {
        fun newInstance(packageModel: PackageModel): PackageDetailDialog {
            val dialog = PackageDetailDialog()
            val args = Bundle()
            args.putParcelable("package", packageModel)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageModel = arguments?.getParcelable("package") ?: return
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPackageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPackageDetails()

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupPackageDetails() {
        binding.apply {
            tvPackageName.text = packageModel.name
            tvPackageDescription.text = packageModel.description

            // Format price
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            tvPackagePrice.text = formatter.format(packageModel.price).replace("IDR", "Rp")

            // Duration
            tvPackageDuration.text = packageModel.getDurationText()

            // Price per month
            val pricePerMonth = packageModel.getPricePerMonth()
            tvPricePerMonth.text = "~${formatter.format(pricePerMonth).replace("IDR", "Rp")}/month"

            // Status
            tvPackageStatus.text = if (packageModel.isActive) "ACTIVE" else "INACTIVE"
            tvPackageStatus.setTextColor(
                if (packageModel.isActive)
                    requireContext().getColor(android.R.color.holo_green_dark)
                else
                    requireContext().getColor(android.R.color.holo_red_dark)
            )

            // Subscriber count
            tvSubscriberCount.text = "${packageModel.subscriberCount} active subscribers"

            // Features
            val featuresText = packageModel.features.mapIndexed { index, feature ->
                "✓ $feature"
            }.joinToString("\n")
            tvFeaturesList.text = featuresText

            // Dates
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            tvCreatedDate.text = "Created: ${dateFormat.format(packageModel.createdAt)}"
            tvUpdatedDate.text = "Last updated: ${dateFormat.format(packageModel.updatedAt)}"

            // Revenue estimation
            val estimatedRevenue = packageModel.subscriberCount * packageModel.price
            tvEstimatedRevenue.text = "Estimated revenue: ${formatter.format(estimatedRevenue).replace("IDR", "Rp")}"
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
