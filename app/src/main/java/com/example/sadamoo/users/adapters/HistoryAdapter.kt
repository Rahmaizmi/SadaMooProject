package com.example.sadamoo.users.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sadamoo.R
import com.example.sadamoo.databinding.ItemHistoryBinding
import com.example.sadamoo.databinding.ItemHistoryConsultationBinding
import com.example.sadamoo.users.data.Consultation
import com.example.sadamoo.users.data.Detection
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private var items: List<Any>,
    private val onItemClick: (Any) -> Unit,
    private val onDeleteClick: (Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_DETECTION = 0
        const val VIEW_TYPE_CONSULTATION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Detection -> VIEW_TYPE_DETECTION
            is Consultation -> VIEW_TYPE_CONSULTATION
            else -> VIEW_TYPE_DETECTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DETECTION -> {
                val binding = ItemHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DetectionViewHolder(binding)
            }
            VIEW_TYPE_CONSULTATION -> {
                val binding = ItemHistoryConsultationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ConsultationViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DetectionViewHolder -> holder.bind(items[position] as Detection)
            is ConsultationViewHolder -> holder.bind(items[position] as Consultation)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class DetectionViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(detection: Detection) {
            binding.apply {
                val resultType = when {
                    detection.cattleType == "Tidak Dapat Mendeteksi" -> "undetected"
                    detection.isHealthy && detection.detectedDisease == null -> "cattle_type"
                    else -> "disease"
                }

                tvTitle.text = when (resultType) {
                    "undetected" -> "Tidak Dapat Mendeteksi"
                    "cattle_type" -> detection.cattleType
                    "disease" -> detection.detectedDisease ?: "Penyakit Tidak Dikenal"
                    else -> detection.cattleType
                }

                tvSubtitle.text = when (resultType) {
                    "undetected" -> "Tidak Dapat Mendeteksi"
                    "cattle_type" -> "Sapi Sehat"
                    "disease" -> "Terdeteksi Penyakit"
                    else -> "Status Tidak Dikenal"
                }

                tvSubtitle.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        when (resultType) {
                            "cattle_type" -> R.color.green_dark
                            "disease" -> R.color.red_dark
                            else -> R.color.gray_dark
                        }
                    )
                )

                // Hide severity and confidence display
                tvSeverity.visibility = View.GONE
                tvConfidence.visibility = View.VISIBLE
                tvConfidence.text = "${"%.1f".format(detection.confidence * 100)}%"

                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                tvDate.text = dateFormat.format(detection.timestamp)

                ivIcon.setImageResource(R.drawable.ic_scan)

                // Click listener - gunakan itemView.setOnClickListener
                itemView.setOnClickListener { onItemClick(detection) }
            }
        }
    }

    inner class ConsultationViewHolder(private val binding: ItemHistoryConsultationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(consultation: Consultation) {
            binding.apply {
                tvTitle.text = "Konsultasi dengan ${consultation.doctorName}"
                tvSubtitle.text = consultation.lastMessage

                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                tvDate.text = dateFormat.format(consultation.timestamp)

                ivIcon.setImageResource(R.drawable.ic_profile)

                // Click listener - gunakan itemView.setOnClickListener
                itemView.setOnClickListener { onItemClick(consultation) }
            }
        }
    }
}