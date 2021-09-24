package com.misaengfly.chordbox

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import timber.log.Timber
import java.io.IOException


class PlayService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    companion object {
        private const val PLAY_NOTIFICATION_ID = 1002
    }

    private val channelId = "PlayChannel"
    private val notificationManager: NotificationManager
        get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() = NotificationChannel(
        channelId,
        "PlayMusicChannel",
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "PlayMusic"
    }


    private val binder = PlayBinder()
    inner class PlayBinder() : Binder() {
        fun getService(): PlayService = this@PlayService
    }

    override fun onBind(intent: Intent?): IBinder? {
        initService()

        return binder
    }

    private fun initService() {

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
            // register Default Notification Channel
            notificationManager.createNotificationChannel(createNotificationChannel())

            builder = NotificationCompat.Builder(this, channelId)
                .setContentTitle("음악 실행 중")
                .setContentText("test")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_music_note)
                .setOngoing(true) // 사용자가 직접 못지우게 설정
                .setPriority(NotificationCompat.PRIORITY_LOW)
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    /**
     * Player 설정
     **/
    private var player: MediaPlayer? = null
    private var curTime: Int = 0

    fun startPlaying() {
        curTime = 0
        player = MediaPlayer().apply {
            try {
//                setAudioAttributes(
//                    AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .build()
//                )
//                setDataSource(filePath)
                setDataSource("${filesDir?.absolutePath}/musicrecord0.m4a")
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }
        }

        player?.apply {
            setOnPreparedListener(this@PlayService)
            prepareAsync()
        }
    }

    fun pausePlaying() {
        player?.pause()
        curTime = player?.currentPosition ?: 0
    }

    fun resumePlaying() {
        player?.seekTo(curTime)
        player?.start()
    }

    fun stopPlaying() {
        player?.apply {
            stop()
            release()
        }
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
    }

    /**
     * MediaPlayer가 재생 준비를 완료하면 호출
     **/
    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }

    /**
     * MediaPlayer에서 음악 재생이 끝난 경우 이벤트 처리
     **/
    override fun onCompletion(p0: MediaPlayer?) {
        TODO("Not yet implemented")
    }
}