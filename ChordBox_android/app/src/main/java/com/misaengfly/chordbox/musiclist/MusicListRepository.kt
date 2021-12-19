package com.misaengfly.chordbox.musiclist

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.misaengfly.chordbox.database.ChordDatabase
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

    suspend fun deleteRecord(filePath: String) {
        withContext(Dispatchers.IO) {
            database.recordDao.clearRecord(filePath)
        }
    }

}