package com.misaengfly.chordbox

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.misaengfly.chordbox.musiclist.MusicItem

@BindingAdapter("title")
fun TextView.setTitle(item: MusicItem) {
    item?.let {
        if (it.type == MusicType.RECORD)
            text = it.fileName
        else if (it.type == MusicType.URL)
            text = it.url
    }
}