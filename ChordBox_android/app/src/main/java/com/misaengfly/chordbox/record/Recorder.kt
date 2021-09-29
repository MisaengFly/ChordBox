package com.misaengfly.chordbox.record

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import com.github.squti.androidwaverecorder.WaveConfig
import com.github.squti.androidwaverecorder.WaveRecorder

class Recorder private constructor(context: Context) {

    var onStart: (() -> Unit)? = null
    var onStop: (() -> Unit)? = null
    var onPause: (() -> Unit)? = null
    var onAmpListener: ((Int) -> Unit)? = null
        set(value) {
            recorder.onAmplitudeListener = value
            field = value
        }
    var onTimeElapsed: ((Long) -> Unit)? = null
        set(value) {
            recorder.onTimeElapsed = value
            field = value
        }

    private var startTime: Long = 0
    private val recordingConfig = WaveConfig().apply {
        sampleRate = 44100
        audioEncoding = AudioFormat.ENCODING_PCM_16BIT
    }
    private val appContext = context.applicationContext
    private lateinit var recorder: WaveRecorder

    var isRecording = false
        private set
    var isPaused = false
        private set

    fun init(): Recorder {
//        recordingConfig.sampleRate = 44100
//        recordingConfig.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        recorder = WaveRecorder(appContext.recordFile.toString())
            .apply { waveConfig = recordingConfig }

        recorder.onTimeElapsed
        return this
    }

    fun toggleRecording() {
        isPaused = if (isPaused) {
            resumeRecording()
            false
        } else {
            startRecording()
            true
        }
        isRecording = true
    }

    private fun startRecording() {
        startTime = System.currentTimeMillis()
        recorder.startRecording()
        onStart?.invoke()
    }

    private fun resumeRecording() {
        recorder.resumeRecording()
    }

    fun pauseRecording() {
        recorder.pauseRecording()
        isPaused = true
        isRecording = false
        onPause?.invoke()
    }

    fun stopRecording() {
        recorder.stopRecording()
        isPaused = false
        isRecording = false
        onStop?.invoke()
    }

    fun getCurrentTime() = System.currentTimeMillis() - startTime

    val bufferSize: Int
        get() = AudioRecord.getMinBufferSize(
            recordingConfig.sampleRate,
            recordingConfig.channels,
            recordingConfig.audioEncoding
        )

    val tickDuration: Int
        get() = (bufferSize.toDouble() * 1000 / byteRate).toInt()

    private val channelCount: Int
        get() = if (recordingConfig.channels == AudioFormat.CHANNEL_IN_MONO) 1 else 2

    private val byteRate: Long
        get() = (bitPerSample * recordingConfig.sampleRate * channelCount / 8).toLong()

    private val bitPerSample: Int
        get() = when (recordingConfig.audioEncoding) {
            AudioFormat.ENCODING_PCM_8BIT -> 8
            AudioFormat.ENCODING_PCM_16BIT -> 16
            else -> 16
        }

    fun release() {
        onStart = null
        onStop = null
        onPause = null
        recorder.onAmplitudeListener = null
        recorder.onTimeElapsed = null
    }

    companion object : SingletonHolder<Recorder, Context>(::Recorder)
}