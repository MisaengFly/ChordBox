package com.misaengfly.chordbox.musiclist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.misaengfly.chordbox.database.*
import com.misaengfly.chordbox.player.UrlItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicListRepository(private val database: ChordDatabase) {
    val chordList: LiveData<List<MusicItem>> =
        Transformations.map(database.recordDao.getRecords()) {
            it.asDomainModel()
        }

    val urlList: LiveData<List<MusicItem>> =
        Transformations.map(database.urlDao.getUrlFiles()) {
            it.asDomainModel()
        }

    /**
     * RecordDao
     * */
    suspend fun deleteRecord(filePath: String) {
        withContext(Dispatchers.IO) {
            database.recordDao.clearRecord(filePath)
        }
    }

    /**
     * UrlDao
     * */
    suspend fun insertUrl(urlFile: UrlFile) {
        withContext(Dispatchers.IO) {
            database.urlDao.insert(urlFile)
        }
    }

    suspend fun deleteUrl(url: String) {
        withContext(Dispatchers.IO) {
            database.urlDao.clearUrl(url)
        }
    }

    suspend fun findUrl(url: String): UrlItem? {
        return database.urlDao.getUrlFile(url)?.asUrlItem()
    }

    suspend fun updateUrl(chords: String, times: String, url: String) =
        withContext(Dispatchers.IO) {
            database.urlDao.updateUrl(chords, times, url)
        }
}