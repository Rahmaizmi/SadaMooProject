package com.example.sadamoo.admin.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.R
import com.example.sadamoo.admin.models.DiseaseModel
import com.example.sadamoo.databinding.ItemDiseaseAdminBinding
import com.example.sadamoo.databinding.ItemDiseaseBinding
import java.text.SimpleDateFormat
import java.util.*

class DiseaseAdapter(
    private val diseases: List<DiseaseModel>,
    private val onDiseaseClick: (DiseaseModel) -> Unit,
    private val onEditDisease: (DiseaseModel) -> Unit,
    private val onToggleStatus: (DiseaseModel) -> Unit,
    private val onDeleteDisease: (DiseaseModel) -> Unit
) : RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder>() {

    inner class DiseaseViewHolder(private val binding: ItemDiseaseAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(disease: DiseaseModel) {
            binding.apply {
                tvDiseaseName.text = disease.name
                tvScientificName.text = disease.scientificName
                tvDiseaseDescription.text = disease.description


                // Severity
                tvSeverity.text = disease.getSeverityText()
                tvSeverity.setBackgroundColor(Color.parseColor(disease.getSeverityColor()))  // Background warna sesuai severity

                // Contagious status
                tvContagious.text = disease.getContagiousText()
                tvContagious.setTextColor(
                    if (disease.contagious) Color.parseColor("#F44336")
                    else Color.parseColor("#4CAF50")
                )

                // Recovery time
                tvRecoveryTime.text = "Recovery: ${disease.recoveryTime}"

                // Estimated loss
                tvEstimatedLoss.text = "Loss: ${disease.getFormattedLoss()}"

                // Detection count
                tvDetectionCount.text = "${disease.detectionCount} detections"

                // Symptoms (show first 3)
                val symptomsText = disease.symptoms.take(3).joinToString("\n") { "• $it" }
                val remainingSymptoms = disease.symptoms.size - 3
                tvSymptoms.text = if (remainingSymptoms > 0) {
                    "$symptomsText\n• +$remainingSymptoms more symptoms"
                } else {
                    symptomsText
                }

                // Status
                setupDiseaseStatus(disease)

                // Last updated
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                tvLastUpdated.text = "Updated: ${dateFormat.format(disease.updatedAt)}"

                // Click listeners
                root.setOnClickListener { onDiseaseClick(disease) }
                btnEditDisease.setOnClickListener { onEditDisease(disease) }
                btnToggleStatus.setOnClickListener { onToggleStatus(disease) }
                btnDeleteDisease.setOnClickListener { onDeleteDisease(disease) }
            }
        }

        private fun setupDiseaseStatus(disease: DiseaseModel) {
            binding.apply {
                if (disease.isActive) {
                    tvDiseaseStatus.text = "ACTIVE"
                    tvDiseaseStatus.background = itemView.context.getDrawable(R.drawable.disease_status_active)
                    tvDiseaseStatus.setTextColor(Color.parseColor("#4CAF50"))

                    btnToggleStatus.text = "Deactivate"
                    btnToggleStatus.background = itemView.context.getDrawable(R.drawable.button_deactivate_disease)

                    // Show detection count only for active diseases
                    tvDetectionCount.visibility = View.VISIBLE
                } else {
                    tvDiseaseStatus.text = "INACTIVE"
                    tvDiseaseStatus.background = itemView.context.getDrawable(R.drawable.disease_status_inactive)
                    tvDiseaseStatus.setTextColor(Color.parseColor("#F44336"))

                    btnToggleStatus.text = "Activate"
                    btnToggleStatus.background = itemView.context.getDrawable(R.drawable.button_activate_disease)

                    // Hide detection count for inactive diseases
                    tvDetectionCount.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val binding = ItemDiseaseAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DiseaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        holder.bind(diseases[position])
    }

    override fun getItemCount(): Int = diseases.size
}
