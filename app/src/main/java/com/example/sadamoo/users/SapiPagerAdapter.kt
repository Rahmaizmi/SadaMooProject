package com.example.sadamoo.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sadamoo.databinding.ItemSapiPagerBinding

class SapiPagerAdapter(
    private val sapiList: List<Pair<String, Int>>
) : RecyclerView.Adapter<SapiPagerAdapter.SapiViewHolder>() {

    class SapiViewHolder(val binding: ItemSapiPagerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SapiViewHolder {
        val binding = ItemSapiPagerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SapiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SapiViewHolder, position: Int) {
        val (nama, gambar) = sapiList[position]
        holder.binding.imgSapiPager.setImageResource(gambar)
        holder.binding.tvNamaSapiPager.text = nama
    }

    override fun getItemCount() = sapiList.size
}
