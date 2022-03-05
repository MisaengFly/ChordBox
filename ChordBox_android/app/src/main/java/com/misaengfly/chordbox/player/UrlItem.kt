package com.misaengfly.chordbox.player

import com.misaengfly.chordbox.MusicType

data class UrlItem(
    val type: MusicType,
    val url: String,
    val absolutePath: String,
    val urlName: String,
    val duration: String,
    val lastModified: String,
    val chordMap: Map<Int, String>
)
