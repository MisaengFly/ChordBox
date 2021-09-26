package com.misaengfly.chordbox.player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.misaengfly.chordbox.record.Recorder
import com.misaengfly.chordbox.record.SingletonHolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioPlayer private constructor(context: Context) {

    private val appContext = context.applicationContext

    var onProgress: ((Long, Boolean) -> Unit)? = null
    val tickDuration = Recorder.getInstance(this.appContext).tickDuration

    private var isPrepared = false

    private var player: MediaPlayer? = null

    fun init(filePath: String): AudioPlayer {
        player?.release()
        isPrepared = false

        player = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                setOnPreparedListener {
                    isPrepared = true
                }
                prepare()
            } catch (e: IOException) {
                Log.e("AudioPlayer", "prepare() failed")
            }
        }
        return this
    }

    fun togglePlay() {
        if (!player!!.isPlaying) {
            resume()
        } else {
            pause()
        }
    }

    fun seekTo(time: Int) {
        player?.seekTo(time)
    }

    fun resume() {
        player?.start()
        updateProgress()
    }

    fun pause() {
        player?.pause()
        updateProgress()
    }

    fun release() {
        player?.release()
    }

    private fun updateProgress(position: Int = player?.currentPosition ?: 0) {
        onProgress?.invoke(position.toLong(), isPrepared)
    }
//
//    @Suppress("BlockingMethodInNonBlockingContext")
//    suspend fun loadAmps(): List<Int> = withContext(IO) {
//        val amps = mutableListOf<Int>()
//        val buffer = ByteArray(bufferSize)
//        File(recordFile.toString()).inputStream().use {
//            it.skip(WAVE_HEADER_SIZE.toLong())
//
//            var count = it.read(buffer)
//            while (count > 0) {
//                amps.add(buffer.calculateAmplitude())
//                count = it.read(buffer)
//            }
//        }
//        amps
//    }

    private fun ByteArray.calculateAmplitude(): Int {
        return ShortArray(size / 2).let {
            ByteBuffer.wrap(this)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .get(it)
            it.maxOrNull()?.toInt() ?: 0
        }
    }

    private fun reset() {
        player?.prepare()
        player?.pause()
        player?.seekTo(0)
    }

    companion object : SingletonHolder<AudioPlayer, Context>(::AudioPlayer) {
        private const val LOOP_DURATION = 20L
        private val TAG = AudioPlayer::class.simpleName
    }
}