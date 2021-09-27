package com.misaengfly.chordbox.musiclist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.misaengfly.chordbox.databinding.ItemMusicListBinding

class MusicAdapter(private val clickListener: MusicItemListener) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    var data = listOf<MusicItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MusicItemListener(private val clickListener: (filePath: String) -> Unit) {
        fun onClick(music: MusicItem) = clickListener(music.absolutePath)
    }

    class ViewHolder private constructor(private val binding: ItemMusicListBinding) :
        RecyclerView.ViewHolder(binding.root) {

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