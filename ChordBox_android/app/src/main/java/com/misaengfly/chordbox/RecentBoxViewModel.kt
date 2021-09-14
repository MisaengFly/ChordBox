package com.misaengfly.chordbox

import android.app.Application
import android.media.MediaMetadataRetriever
import androidx.lifecycle.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecentBoxViewModel(application: Application) : AndroidViewModel(application) {
    private val mApplication = application

    private val _musicList = MutableLiveData<List<MusicItem>>()
    val musicList: LiveData<List<MusicItem>>
        get() = _musicList

    init {
        _musicList.postValue(getFileList(application))
    }

    fun updateFiles() {
        _musicList.postValue(getFileList(mApplication))
    }

    /**
     * 저장소에서 파일 불러와서 변환
     * */
    private fun getFileList(application: Application): ArrayList<MusicItem> {
        // 파일 얻어오기
        val fileList = application.filesDir.listFiles()
        val musicItemList = arrayListOf<MusicItem>()

        for (file in fileList) {
            musicItemList.add(
                MusicItem(
                    file.absolutePath,
                    file.name,
                    getFileDuration(file),
                    convertLongToDateTime(file.lastModified())
                )
            )
        }
        return musicItemList
    }

    /**
     * 파일 재생 시간 구하기
     * */
    private fun getFileDuration(file: File): String {
        var durationString = ""

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMilliSec = time!!.toLong()
        val duration = timeInMilliSec / 1000
        val hours = duration / 3600
        val minutes = (duration - hours * 3600) / 60
        val seconds = duration - (hours * 3600 + minutes * 60)

        if (hours > 0) {
            durationString += hours.toInt()
            durationString += ":"
        }
        durationString += "${minutes.toInt()}:${seconds.toInt()}"

        return durationString
    }

    private fun convertLongToDateTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }

    /**
     * Click 시 코드(Chord) 페이지로 이동
     * true일 경우 이동
     * */
    private val _moveToMusicChord = MutableLiveData<Boolean>()
    val moveToMusicChord : LiveData<Boolean> get() = _moveToMusicChord

    fun doMoveToMusicChord() {
        _moveToMusicChord.value = true
    }
}