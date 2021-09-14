package com.misaengfly.chordbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.misaengfly.chordbox.databinding.ItemMusicListBinding

class RecentBoxAdapter(private val clickListener: MusicItemListener) : RecyclerView.Adapter<RecentBoxAdapter.ViewHolder>() {

    var data = listOf<MusicItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MusicItemListener(private val clickListener: () -> Unit) {
        fun onClick(music: MusicItem) = clickListener()
    }

    class ViewHolder private constructor(private val binding: ItemMusicListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MusicItem, clickListener: MusicItemListener) {
            binding.music = item
            binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemMusicListBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], clickListener)
    }

    override fun getItemCount(): Int = data.size
}