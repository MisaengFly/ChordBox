package com.misaengfly.chordbox.player

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.ItemUrlChordBinding

class UrlChordAdapter : RecyclerView.Adapter<UrlChordAdapter.ViewHolder>() {
    var data = mapOf<Int, String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    
    var selectedPosition = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder private constructor(val binding: ItemUrlChordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chord: String) {
            binding.itemUrlTv.apply {
                text = chord
                background = ColorDrawable(Color.TRANSPARENT)
            }
        }

        fun bindSelected(chord: String) {
            binding.itemUrlTv.apply {
                text = chord
                background = ContextCompat.getDrawable(
                    binding.root.context, R.drawable.bg_url_selected_item
                )
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemUrlChordBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val param = if (data.containsKey(position)) data[position]!! else ""

        if (selectedPosition == position)
            holder.bindSelected(param)
        else
            holder.bind(param)
    }

    override fun getItemCount(): Int = data.size
}