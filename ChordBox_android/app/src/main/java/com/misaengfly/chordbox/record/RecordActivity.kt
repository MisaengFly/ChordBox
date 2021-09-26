package com.misaengfly.chordbox.record

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.databinding.ActivityRecordBinding
import com.misaengfly.chordbox.dialog.StopDialog

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class RecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordBinding
    private lateinit var recorder: Recorder

    /**
     * 퍼미션 요청
     * RECORD_AUDIO
     **/
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 퍼미션 요청
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        // 버튼 이벤트 연결
        binding.recordBtn.setOnClickListener {
            recorder.toggleRecording()
        }
        binding.pauseBtn.setOnClickListener {
            recorder.pauseRecording()
        }
        binding.stopBtn.setOnClickListener {
            recorder.stopRecording()
        }
    }

    override fun onStart() {
        super.onStart()
        listenOnRecorderStates()
    }

    override fun onStop() {
        recorder.release()
        super.onStop()
    }

    private fun listenOnRecorderStates() = with(binding) {
        recorder = Recorder.getInstance(applicationContext).init().apply {
            onStart = {

            }
            onStop = {
                recordTimeView.text = 0L.formatAsTime()
                StopDialog().show(supportFragmentManager, "StopDialog")
            }
            onPause = {

            }
            onAmpListener = {
            }
            onTimeElapsed = {
                runOnUiThread {
                    if (recorder.isRecording) {
                        recordTimeView.text = (it*1000).formatAsTime()
                    }
                }
            }
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.choose_source_separation ->
                    if (checked) {
                        // Pirates are the best
                    }
                R.id.choose_chord_recognition ->
                    if (checked) {
                        // Ninjas rule
                    }
            }
        }
    }
}