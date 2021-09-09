package com.misaengfly.chordbox

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Binder
import android.os.IBinder
import android.util.Log
import timber.log.Timber
import java.io.IOException

class RecordService : Service() {

    private var filePath: String = ""
    private var recorder: MediaRecorder? = null

    private var state = State.BEFORE_RECORDING

    private val binder = RecordBinder()

    inner class RecordBinder : Binder() {
        fun getService(): RecordService = this@RecordService
    }

    override fun onBind(intent: Intent): IBinder {
        filePath = intent.getStringExtra("filePath").toString()
        return binder
    }

    fun startRecording() {

        /** 그대로 저장하면 용량이 크다.
         * 프레임 : 한 순간의 음성이 들어오면, 음성을 바이트 단위로 전부 저장하는 것
         * 초당 15프레임 이라면 보통 8K(8000바이트) 정도가 한순간에 저장됨
         * 따라서 용량이 크므로, 압축할 필요가 있음 *
         **/
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC) // 어디에서 음성 데이터를 받을 것인지
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // 압축 형식 설정
            setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC) // 인코딩 방법 설정
            setAudioEncodingBitRate(384000)
            setAudioSamplingRate(44100)
            setOutputFile(filePath)

            try {
                prepare()
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }

            start()
        }
    }

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }


    /**
     * Player 설정
     **/

    private var player: MediaPlayer? = null

    fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }
}