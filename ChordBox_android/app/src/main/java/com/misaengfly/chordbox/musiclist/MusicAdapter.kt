package com.misaengfly.chordbox.musiclist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.misaengfly.chordbox.databinding.ItemMusicListBinding
import kotlinx.coroutines.channels.ticker

class MusicAdapter(
    private val clickListener: MusicItemListener,
    private val longClickListener: DeleteItemListener
) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    var data = listOf<MusicItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MusicItemListener(private val clickListener: (filePath: String) -> Unit) {
        fun onClick(music: MusicItem) = clickListener(music.absolutePath)
    }

    class DeleteItemListener(longClickListener: (view: View?, item: MusicItem) -> Boolean) {
        val longClickResult = longClickListener
    }

    class ViewHolder private constructor(private val binding: ItemMusicListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MusicItem,
            clickListener: MusicItemListener,
            longClickListener: DeleteItemListener
        ) {
            binding.music = item
            binding.clickListener = clickListener
            binding.musicItemContainer.setOnLongClickListener { view ->
                longClickListener.longClickResult(
                    view!!,
                    item
                )
            }
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
        holder.bind(data[position], clickListener, longClickListener)
    }

    override fun getItemCount(): Int = data.size
}