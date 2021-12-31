package com.misaengfly.chordbox.musiclist

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.database.Record
import com.misaengfly.chordbox.database.UrlFile
import com.misaengfly.chordbox.database.asDomainModel
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

    suspend fun findUrl(url: String): MusicItem? {
        return database.urlDao.getUrlFile(url)?.asDomainModel()
    }
}