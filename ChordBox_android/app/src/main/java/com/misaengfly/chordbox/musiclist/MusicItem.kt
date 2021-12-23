package com.misaengfly.chordbox.musiclist

import com.misaengfly.chordbox.MusicType

data class MusicItem(
    val type: MusicType,
    val url: String,
    val absolutePath: String,
    val fileName: String,
    val duration: String,
    val lastModified: String,
    val chordMap: Map<Int, String>
)
