package com.misaengfly.chordbox.delivery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.misaengfly.chordbox.databinding.ItemDeliveryListBinding
import com.misaengfly.chordbox.databinding.ItemMusicListBinding

class BoxDeliveryAdapter : RecyclerView.Adapter<BoxDeliveryAdapter.BoxViewHolder>() {

    class BoxViewHolder private constructor(private val binding: ItemDeliveryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {

        }

        companion object {
            fun from(parent: ViewGroup): BoxViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemDeliveryListBinding.inflate(layoutInflater, parent, false)
                return BoxViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoxViewHolder {
        return BoxViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: BoxViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return 0
    }
}