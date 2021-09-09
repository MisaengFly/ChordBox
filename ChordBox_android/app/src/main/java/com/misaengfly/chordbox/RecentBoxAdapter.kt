package com.misaengfly.chordbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentBoxAdapter : RecyclerView.Adapter<RecentBoxAdapter.ViewHolder>() {

    var data = arrayListOf<MusicItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val musicName = itemView.findViewById<TextView>(R.id.music_title)
        private val musicDuration = itemView.findViewById<TextView>(R.id.music_duration)
        private val musicLastModifiedTime = itemView.findViewById<TextView>(R.id.music_record_time)

        fun bind(item: MusicItem) {
            musicName.text = item.fileName
            musicDuration.text = item.duration
            musicLastModifiedTime.text = item.lastModified
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_music_list, parent, false)
                return ViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}