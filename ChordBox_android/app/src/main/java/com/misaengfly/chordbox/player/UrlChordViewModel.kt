package com.misaengfly.chordbox.player

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.musiclist.MusicListRepository
import com.misaengfly.chordbox.network.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class UrlChordViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = ChordDatabase.getInstance(application)
    private val musicListRepository = MusicListRepository(database)

    val urlItem = MutableLiveData<UrlItem?>()

    /**
     * Record_table의 데이터 업데이트
     **/
    suspend fun updateUrlItem(chords: String, times: String, url: String) =
        withContext(Dispatchers.IO) {
            musicListRepository.updateUrl(chords, times, url)
        }


    suspend fun findUrlItem(url: String) = withContext(Dispatchers.IO) {
        val res = musicListRepository.findUrl(url)
        res?.let {
            urlItem.postValue(it)
            Log.d("log", it.chordMap.size.toString())
        }
        Log.d("log", res.toString())
    }

    /**
     * Factory
     **/
    class Factory(private val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UrlChordViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UrlChordViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}