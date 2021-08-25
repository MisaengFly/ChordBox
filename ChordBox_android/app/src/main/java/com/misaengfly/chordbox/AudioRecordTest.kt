package com.misaengfly.chordbox

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private const val REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 300

class AudioRecordTest : AppCompatActivity() {
    private var fileName: String = ""

    private var recordButton: Button? = null
    private var recorder: MediaRecorder? = null

    private var playButton: Button? = null
    private var player: MediaPlayer? = null

    /**
     * 퍼미션 요청
     * RECORD_AUDIO
     **/
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> =
        arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionToRecordAccepted =
            when (requestCode) {
                REQUEST_RECORD_AUDIO_PERMISSION -> {
                    grantResults[0] > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                } else -> {
                    false
                }
            }
        if (!permissionToRecordAccepted) finish()
    }

    /**
     * 녹음 파트
     **/
    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    var mStartRecording = true
    var recordClicker: OnClickListener = OnClickListener {
        onRecord(mStartRecording)
        (it as Button).text = when (mStartRecording) {
            true -> "Stop recording"
            false -> "Start recording"
        }
        mStartRecording = !mStartRecording
    }

    var mStartPlaying = true
    var startClicker: OnClickListener = OnClickListener {
        onPlay(mStartPlaying)
        (it as Button).text = when (mStartPlaying) {
            true -> "Stop playing"
            false -> "Start playing"
        }
        mStartPlaying = !mStartPlaying
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_audio_record_test)

        // Record to the external cache directory for visibility
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        recordButton = findViewById(R.id.recButton)
        recordButton!!.text = "Start recording"
        recordButton!!.setOnClickListener(recordClicker)

        playButton = findViewById(R.id.playButton)
        playButton!!.text = "Start playing"
        playButton!!.setOnClickListener(startClicker)
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }
}