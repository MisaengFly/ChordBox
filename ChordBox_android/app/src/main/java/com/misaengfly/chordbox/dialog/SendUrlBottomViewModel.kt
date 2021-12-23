package com.misaengfly.chordbox.dialog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.database.UrlFile
import com.misaengfly.chordbox.musiclist.MusicListRepository
import kotlinx.coroutines.launch
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
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-mm-dd hh:mm")
        val curTime = dateFormat.format(Date(time))

        viewModelScope.launch {
            val urlInfo = UrlFile(
                url,
                curTime
            )
            musicListRepository.insertUrl(urlInfo)
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