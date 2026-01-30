package com.example.sadamoo.users.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.databinding.ItemDiseaseBinding
import com.example.sadamoo.users.models.Disease

class DiseaseAdapter(
    private var diseases: List<Disease>,
    private val onItemClick: (Disease) -> Unit
) : RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder>() {

    inner class DiseaseViewHolder(private val binding: ItemDiseaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(disease: Disease) {
            binding.apply {
                tvDiseaseName.text = disease.name
                tvDiseaseDescription.text = disease.description
                tvSeverity.text = disease.severity
                ivDiseaseImage.setImageResource(disease.imageRes)

                // Set text color to WHITE
                tvSeverity.setTextColor(
                    itemView.context.getColor(android.R.color.white)
                )
                tvContagious.setTextColor(
                    itemView.context.getColor(android.R.color.white)
                )

                // Set BACKGROUND COLOR berdasarkan severity
                val severityColor = when (disease.severity) {
                    "Ringan" -> android.graphics.Color.parseColor("#4CAF50")  // Hijau
                    "Sedang" -> android.graphics.Color.parseColor("#FF9800")  // Oren
                    "Berat" -> android.graphics.Color.parseColor("#F44336")   // Merah
                    else -> android.graphics.Color.parseColor("#757575")      // Abu-abu
                }
                tvSeverity.setBackgroundColor(severityColor)

                // Set BACKGROUND COLOR untuk badge contagious
                val contagiousColor = if (disease.isContagious) {
                    android.graphics.Color.parseColor("#4ECDC4")  // Tosca untuk menular
                } else {
                    android.graphics.Color.parseColor("#4CAF50")  // Hijau untuk tidak menular
                }
                tvContagious.setBackgroundColor(contagiousColor)

                // Show contagious indicator
                tvContagious.text = if (disease.isContagious) "Menular" else "Tidak Menular"

                root.setOnClickListener {
                    onItemClick(disease)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val binding = ItemDiseaseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DiseaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        holder.bind(diseases[position])
    }

    override fun getItemCount(): Int = diseases.size

    fun updateData(newDiseases: List<Disease>) {
        diseases = newDiseases
        notifyDataSetChanged()
    }
}
