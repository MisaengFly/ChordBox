package com.misaengfly.chordbox

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.misaengfly.chordbox.databinding.ActivityMusicRecordBinding
import timber.log.Timber

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MusicRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMusicRecordBinding

    private var filePath: String = ""

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

    private lateinit var recordService: RecordService
    private var bound: Boolean = false

    /** service binding을 위한 callbacks 정의하고 bindService()로 전달 */
    private val connection = object : ServiceConnection {
        // Called when the connection with the service is established
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as RecordService.RecordBinder
            recordService = binder.getService()
            bound = true
        }

        // Called when the connection with the service disconnects unexpectedly
        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMusicRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        binding.recordBtn.setOnClickListener {
            recordService.startRecording()
        }

        binding.stopBtn.setOnClickListener {
            recordService.stopRecording()
        }

        binding.playbutton.setOnClickListener {
            recordService.onPlay(true)
        }

        binding.stopbutton.setOnClickListener {
            recordService.onPlay(false)
        }
    }

    override fun onStart() {
        super.onStart()
//        filePath = "${externalCacheDir?.absolutePath}/musicrecord.m4a"

        val fileCount = filesDir.listFiles { _, name ->
            name.contains("musicrecord")
        }.size

        filePath = "${filesDir?.absolutePath}/musicrecord${fileCount}.m4a"
        Timber.d("filesDir : ${filesDir?.absolutePath}")
        // RecordService 를 Bind
        Intent(this, RecordService::class.java).also { intent: Intent ->
            intent.putExtra("filePath", filePath)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        bound = false
    }
}