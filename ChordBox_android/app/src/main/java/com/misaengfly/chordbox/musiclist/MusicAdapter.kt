package com.misaengfly.chordbox.musiclist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.misaengfly.chordbox.MusicType
import com.misaengfly.chordbox.databinding.ItemMusicListBinding

private const val ITEM_VIEW_FILE_HEADER = 0
private const val ITEM_VIEW_FILE_ITEM = 1
private const val ITEM_VIEW_URL_HEADER = 2
private const val ITEM_VIEW_URL_ITEM = 3

class MusicAdapter(
    private val clickListener: MusicItemListener,
    private val longClickListener: DeleteItemListener
) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    private var fileDataSize = 0
    private var urlDataSize = 0

    var data = listOf<MusicItem>()
        set(value) {
            field = value
            for (i in value.indices) {
                if (value[i].type == MusicType.URL) {
                    fileDataSize = i
                    break
                }
            }
            urlDataSize = value.size
            notifyDataSetChanged()
        }

    class MusicItemListener(private val clickListener: (item: MusicItem) -> Unit) {
        fun onClick(music: MusicItem) = clickListener(music)
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

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> ITEM_VIEW_FILE_HEADER
            position < fileDataSize -> ITEM_VIEW_FILE_ITEM
            position == fileDataSize -> ITEM_VIEW_URL_HEADER
            position < urlDataSize -> ITEM_VIEW_URL_ITEM
            else -> throw ClassCastException("Unknown viewType $position")
        }
    }
}