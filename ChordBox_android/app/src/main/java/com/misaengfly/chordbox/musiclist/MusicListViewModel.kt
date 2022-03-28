package com.misaengfly.chordbox.musiclist

import android.app.Application
import android.media.MediaMetadataRetriever
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.misaengfly.chordbox.CombinedLiveData
import com.misaengfly.chordbox.MusicType
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.record.convertLongToDateTime
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter

class MusicListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ChordDatabase.getInstance(application)
    private val musicListRepository = MusicListRepository(database)

    val chordList = musicListRepository.chordList
    val urlList = musicListRepository.urlList

    private var _musicList = CombinedLiveData(chordList, urlList) { data1, data2 ->
        val ret = mutableListOf<MusicItem>()
        data1?.let { ret.addAll(it) }
        data2?.let { ret.addAll(it) }
        ret
    }
    val musicList: LiveData<List<MusicItem>>
        get() = _musicList


    fun updateMusicList() {
        _musicList = CombinedLiveData(chordList, urlList) { data1, data2 ->
            val ret = mutableListOf<MusicItem>()
            data1?.let { ret.addAll(it) }
            data2?.let { ret.addAll(it) }
            ret
        }
    }

    fun removeFile(filePath: String) {
        viewModelScope.launch {
            File(filePath).delete()
            musicListRepository.deleteRecord(filePath)
            updateMusicList()
        }
    }

    fun removeUrl(url: String) {
        viewModelScope.launch {
            val urlInfo = musicListRepository.findUrl(url)
            urlInfo.value?.let {
                val file = File(
                    getApplication<Application>().applicationContext.getExternalFilesDir(null),
                    it.absolutePath
                )
                file.delete()
            }
            musicListRepository.deleteUrl(url)
            updateMusicList()
        }
    }

    /**
     * 저장소에서 파일 불러와서 변환
     * */
    private fun getWavFileList(application: Application): ArrayList<MusicItem> {
        // 파일 얻어오기 - wav 확장자인 파일만 가능
        val fileList = application.filesDir.listFiles(FileFilter {
            it.extension == "wav"
        })
        val musicItemList = arrayListOf<MusicItem>()

//        for (file in fileList) {
//            musicItemList.add(
//                MusicItem(
//                    MusicType.RECORD,
//                    file.absolutePath,
//                    file.name,
//                    getFileDuration(file),
//                    file.lastModified().convertLongToDateTime(),
//                    mapOf()
//                )
//            )
//        }
        return musicItemList
    }

    /**
     * Click 시 코드(Chord) 페이지로 이동
     * true일 경우 이동
     * */
    private val _moveToMusicChord = MutableLiveData<Boolean>()
    val moveToMusicChord: LiveData<Boolean> get() = _moveToMusicChord

    fun doMoveToMusicChord() {
        _moveToMusicChord.value = true
    }
}