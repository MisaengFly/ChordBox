package com.misaengfly.chordbox.record

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.misaengfly.chordbox.MainActivity
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.State
import timber.log.Timber
import java.io.IOException


class RecordService : Service() {

    companion object {
        private const val RECORD_NOTIFICATION_ID = 1001
    }

    private var filePath: String = ""
    private var recorder: MediaRecorder? = null

    private var state = State.BEFORE_RECORDING

    private val binder = RecordBinder()

    private lateinit var builder: NotificationCompat.Builder

    inner class RecordBinder : Binder() {
        fun getService(): RecordService = this@RecordService
    }

    override fun onBind(intent: Intent): IBinder {
        filePath = intent.getStringExtra("filePath").toString()

        createNotificationChannel()

        return binder
    }

    private fun createNotificationChannel() {
        val channelId = getString(R.string.record_channel_id)

        // Create an explicit intent for an Activity in your app
        val intent = Intent(baseContext, MusicRecordActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0, intent, 0
        )

        // API 26 이상부터 NotificationChannel 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.record_channel_name)
            val descriptionText = getString(R.string.record_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            // Notification 과 채널 연결
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            baseContext, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("textTitle")
            .setContentText("textContent")
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 사용자가 직접 못지우게 설정
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(fullScreenPendingIntent, true)
        //            .setAutoCancel(false)
    }

    fun startRecording() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(RECORD_NOTIFICATION_ID, builder.build())

        /** 그대로 저장하면 용량이 크다.
         * 프레임 : 한 순간의 음성이 들어오면, 음성을 바이트 단위로 전부 저장하는 것
         * 초당 15프레임 이라면 보통 8K(8000바이트) 정도가 한순간에 저장됨
         * 따라서 용량이 크므로, 압축할 필요가 있음 *
         **/
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC) // 어디에서 음성 데이터를 받을 것인지
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // 압축 형식 설정
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC) // 인코딩 방법 설정
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

        // Notify 삭제
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}