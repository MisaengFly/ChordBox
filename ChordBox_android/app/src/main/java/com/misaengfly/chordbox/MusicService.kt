package com.misaengfly.chordbox

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.io.IOException
import kotlin.concurrent.thread


class MusicService : Service(), MediaPlayer.OnPreparedListener {

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 1
    }

    private val channelId = "PlayChannel"

    private val notificationManager: NotificationManager
        get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() = NotificationChannel(
        channelId,
        "PlayMusicChannel",
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "PlayMusic"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startPlaying()

        val notificationIntent = Intent(baseContext, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // Pending Intent를 이용하면 포그라운드 서비스 상태에서 알림을 누르면 앱의 MainActivity를 다시 열게 된다.
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        // 알림 띄우기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification: Notification = Notification.Builder(this, channelId)
                .setContentTitle("음악 실행 중")
                .setContentText("test")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_music_note)
                .setOngoing(true) // 사용자가 직접 못지우게 설정
                .build()

            startForeground(ONGOING_NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        registerDefaultNotificationChannel()
    }

    private fun registerDefaultNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createNotificationChannel())
        }
    }

    /**
     * Player 설정
     **/
    private var player: MediaPlayer? = null

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
//                setAudioAttributes(
//                    AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .build()
//                )
//                setDataSource(filePath)
                setDataSource("${filesDir?.absolutePath}/musicrecord1.m4a")
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }
        }

        player?.apply {
            setOnPreparedListener(this@MusicService)
            prepareAsync()
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
    }

    /**
     * Media Player가 준비되면 호출
     **/
    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }
}