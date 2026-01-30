package com.example.sadamoo.users.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.databinding.ItemCattleTypeBinding
import com.example.sadamoo.users.models.CattleType

class CattleTypeAdapter(private val cattleTypes: List<CattleType>) :
    RecyclerView.Adapter<CattleTypeAdapter.CattleTypeViewHolder>() {

    inner class CattleTypeViewHolder(private val binding: ItemCattleTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cattleType: CattleType) {
            binding.apply {
                tvCattleName.text = cattleType.name
                // Load image - you can use Glide or Picasso here
                // Glide.with(itemView.context).load(cattleType.imageRes).into(ivCattle)

                root.setOnClickListener {
                    // Handle cattle type click
                    // Show detail or navigate to cattle info
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CattleTypeViewHolder {
        val binding = ItemCattleTypeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CattleTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CattleTypeViewHolder, position: Int) {
        holder.bind(cattleTypes[position])
    }

    override fun getItemCount(): Int = cattleTypes.size
}
