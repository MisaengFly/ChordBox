package com.misaengfly.chordbox.chord

import android.app.Application
import android.media.AudioRecord
import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.lifecycle.AndroidViewModel
import timber.log.Timber

class ChordViewModel(application: Application) : AndroidViewModel(application) {
    private val mApplication = application

//    val bufferSize: Int
//        get() = AudioRecord.getMinBufferSize(
//
//        )
//
//    val tickDuration: Int
//        get() = (bufferSize.toDouble() * 1000 / byteRate).toInt()
//
//    private val channelId: Int
//        get() {
//
//        }
//
//    private val bitPerSample: Int get() =
//
//    private val byteRate: Long get() = (bitPerSample * recordingConfig.sampleRate * channelCount / 8).toLong()

}