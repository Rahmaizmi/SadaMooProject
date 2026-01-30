package com.example.sadamoo.admin.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.R
import com.example.sadamoo.admin.models.PackageModel
import com.example.sadamoo.databinding.ItemPackageBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PackageAdapter(
    private val packages: List<PackageModel>,
    private val onPackageClick: (PackageModel) -> Unit,
    private val onEditPackage: (PackageModel) -> Unit,
    private val onToggleStatus: (PackageModel) -> Unit,
    private val onDeletePackage: (PackageModel) -> Unit
) : RecyclerView.Adapter<PackageAdapter.PackageViewHolder>() {

    inner class PackageViewHolder(private val binding: ItemPackageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(packageModel: PackageModel) {
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

                // Subscriber count
                tvSubscriberCount.text = "${packageModel.subscriberCount} subscribers"

                // Features (show first 3)
                val featuresText = packageModel.features.take(3).joinToString("\n") { "• $it" }
                val remainingFeatures = packageModel.features.size - 3
                tvFeatures.text = if (remainingFeatures > 0) {
                    "$featuresText\n• +$remainingFeatures more features"
                } else {
                    featuresText
                }

                // Status
                setupPackageStatus(packageModel)

                // Last updated
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                tvLastUpdated.text = "Updated: ${dateFormat.format(packageModel.updatedAt)}"

                // Click listeners
                root.setOnClickListener { onPackageClick(packageModel) }
                btnEditPackage.setOnClickListener { onEditPackage(packageModel) }
                btnToggleStatus.setOnClickListener { onToggleStatus(packageModel) }
                btnDeletePackage.setOnClickListener { onDeletePackage(packageModel) }
            }
        }

        private fun setupPackageStatus(packageModel: PackageModel) {
            binding.apply {
                if (packageModel.isActive) {
                    tvPackageStatus.text = "ACTIVE"
                    tvPackageStatus.background = itemView.context.getDrawable(R.drawable.package_status_active)
                    tvPackageStatus.setTextColor(Color.parseColor("#4CAF50"))

                    btnToggleStatus.text = "Deactivate"
                    btnToggleStatus.background = itemView.context.getDrawable(R.drawable.button_deactivate)

                    // Show subscriber count only for active packages
                    tvSubscriberCount.visibility = View.VISIBLE
                } else {
                    tvPackageStatus.text = "INACTIVE"
                    tvPackageStatus.background = itemView.context.getDrawable(R.drawable.package_status_inactive)
                    tvPackageStatus.setTextColor(Color.parseColor("#F44336"))

                    btnToggleStatus.text = "Activate"
                    btnToggleStatus.background = itemView.context.getDrawable(R.drawable.button_activate)

                    // Hide subscriber count for inactive packages
                    tvSubscriberCount.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val binding = ItemPackageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PackageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.bind(packages[position])
    }

    override fun getItemCount(): Int = packages.size
}
