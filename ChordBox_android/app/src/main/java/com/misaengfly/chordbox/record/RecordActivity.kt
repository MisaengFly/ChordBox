package com.misaengfly.chordbox.record

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.databinding.ActivityRecordBinding
import com.misaengfly.chordbox.dialog.StopDialog
import com.misaengfly.chordbox.network.FileApi
import com.misaengfly.chordbox.network.FileResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private const val RECORDING = 0
private const val PAUSE = 1
private const val STOP = 2

class RecordActivity : AppCompatActivity(), StopDialog.StopDialogListener {
    private lateinit var binding: ActivityRecordBinding
    private lateinit var recorder: Recorder

    private val viewModel: RecordViewModel by lazy {
        val dataSource = ChordDatabase.getInstance(this.application).recordDao
        val viewModelFactory = RecordViewModelFactory(dataSource, this.application)
        ViewModelProvider(this, viewModelFactory).get(RecordViewModel::class.java)
    }

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

    /**
     * Record Stop And Save Dialog
     **/
    private fun showSaveDialog() {
        val dialog = StopDialog()
        dialog.show(supportFragmentManager, "StopDialog")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val file = File(recorder.filePath)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("audiofile", file.name, requestFile)

        // DB에 저장
        viewModel.insertRecord(file.absolutePath, file.name)

        // 서버로 전송
        FileApi.retrofitService.sendAudioFile(body).enqueue(object : Callback<FileResponse> {
            override fun onResponse(
                call: Call<FileResponse>,
                response: Response<FileResponse>
            ) {
                Log.d("callback success : ", response.message())
            }

            override fun onFailure(call: Call<FileResponse>, t: Throwable) {
                Log.d("callback failure", t.toString())
            }
        })
        dialog.dismiss()

        // 알림창 보여주기
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_info, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.setOnDismissListener {
            finish()
        }
        bottomSheetDialog.show()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // 파일 삭제 하기
        File(recorder.filePath).delete()
        dialog.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 퍼미션 요청
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        // 버튼 이벤트 연결
        binding.recordBtn.setOnClickListener {
            setButton(RECORDING)
        }
        binding.pauseBtn.setOnClickListener {
            setButton(PAUSE)
        }
        binding.stopBtn.setOnClickListener {
            setButton(STOP)
        }
    }

    override fun onStart() {
        super.onStart()
        listenOnRecorderStates()
        // File Name 보여주기
        binding.recordFileNameTv.text = File(recorder.filePath).name
    }

    /**
     * recording 중에 앱 죵료시 녹음 자동 종료 + 파일 삭제
     * */
    override fun onPause() {
        if (recorder.isRecording) {
            recorder.forcedStopRecording()
        }
        super.onPause()
    }

    override fun onStop() {
        recorder.release()
        super.onStop()
    }

    private fun setButton(type: Int) {
        when (type) {
            RECORDING -> {
                recorder.toggleRecording()
                binding.recordBtn.visibility = GONE
                binding.pauseBtn.visibility = VISIBLE
                binding.stopBtn.visibility = VISIBLE
            }
            PAUSE -> {
                binding.recordBtn.visibility = GONE
                if (recorder.isRecording) {
                    recorder.pauseRecording()
                    binding.pauseBtn.setImageDrawable(getDrawable(R.drawable.ic_radio_checked))
                } else {
                    recorder.toggleRecording()
                    binding.pauseBtn.setImageDrawable(getDrawable(R.drawable.ic_pause))
                }
            }
            STOP -> {
                recorder.stopRecording()
                binding.recordBtn.visibility = VISIBLE
                binding.pauseBtn.visibility = GONE
                binding.stopBtn.visibility = GONE
            }
        }
    }

    private fun listenOnRecorderStates() = with(binding) {
        recorder = Recorder.getInstance(applicationContext).init().apply {
            onStart = {

            }
            onStop = {
                recorderVisualizer.clear()
                recordTimeView.text = 0L.formatAsTime()
                showSaveDialog()
            }
            onPause = {

            }
            onAmpListener = {
                runOnUiThread {
                    if (recorder.isRecording) {
                        recorderVisualizer.addAmp(it, tickDuration)
                    }
                }
            }
            onTimeElapsed = {
                runOnUiThread {
                    if (recorder.isRecording) {
                        recordTimeView.text = (it * 1000).formatAsTime()
                    }
                }
            }
            onForcedStop = {
                File(recorder.filePath).delete()
                recorderVisualizer.clear()
                recordTimeView.text = 0L.formatAsTime()
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