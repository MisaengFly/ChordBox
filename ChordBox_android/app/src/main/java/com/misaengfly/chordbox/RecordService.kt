package com.misaengfly.chordbox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber
import java.io.IOException

class RecordService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
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
        val channelId = getString(R.string.channel_id)

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
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
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
            .setAutoCancel(false)
            .setOngoing(true) // 사용자가 직접 못지우게 설정
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(fullScreenPendingIntent, true)
    }

    fun startRecording() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
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
//                setDataSource(filePath)
                setDataSource("${filesDir?.absolutePath}/musicrecord5.m4a")
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