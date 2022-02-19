package com.misaengfly.chordbox.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.musiclist.MusicItem
import com.misaengfly.chordbox.musiclist.MusicListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

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