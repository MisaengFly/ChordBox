package com.misaengfly.chordbox.musiclist

data class MusicItem(
    val absolutePath: String,
    val fileName: String,
    val duration: String,
    val lastModified: String
)
