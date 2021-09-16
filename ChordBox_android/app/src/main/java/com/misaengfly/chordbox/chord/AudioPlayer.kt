package com.misaengfly.chordbox.chord

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat

class AudioPlayer private constructor(context: Context){

    var filePath: String = "${context.filesDir?.absolutePath}/musicrecord2.m4a"
    private val mediaExtractor = MediaExtractor()

    private lateinit var mediaFormat: MediaFormat
    private val channelCount: Int =  mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

    init {

    }

    fun init() {

        val numTracks = mediaExtractor.trackCount
        for (i in 0 until numTracks) {
            mediaFormat = mediaExtractor.getTrackFormat(i)
            if (mediaFormat.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                mediaExtractor.selectTrack(i)
                break
            }
        }

    }
}