package com.misaengfly.chordbox.dialog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.database.UrlFile
import com.misaengfly.chordbox.musiclist.MusicItem
import com.misaengfly.chordbox.musiclist.MusicListRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class SendUrlBottomViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = ChordDatabase.getInstance(application)
    private val musicListRepository = MusicListRepository(database)

    /**
     * DB에 저장
     * */
    fun insertUrlToDB(url: String) {
        val date = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val curTime = dateFormat.format(date)

        viewModelScope.launch {
            val urlInfo = UrlFile(
                url,
                curTime
            )
            musicListRepository.insertUrl(urlInfo)
        }
    }

    fun isExistUrl(url: String): Boolean {
        return runBlocking {
            val musicItem = musicListRepository.findUrl(url)
            musicItem != null
        }
    }

    /**
     * Factory
     **/
    class Factory(private val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SendUrlBottomViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SendUrlBottomViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}