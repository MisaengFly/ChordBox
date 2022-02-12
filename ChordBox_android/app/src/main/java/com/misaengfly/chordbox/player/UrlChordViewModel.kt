package com.misaengfly.chordbox.player

import android.app.Application
import androidx.lifecycle.*
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.musiclist.MusicItem
import com.misaengfly.chordbox.musiclist.MusicListRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UrlChordViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = ChordDatabase.getInstance(application)
    private val musicListRepository = MusicListRepository(database)

    /**
     * DB에서 로드
     * */
    fun findUrlItem(url: String): MusicItem? {
        return runBlocking {
            musicListRepository.findUrl(url)
        }
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