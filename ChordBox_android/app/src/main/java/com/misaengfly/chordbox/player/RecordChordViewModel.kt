package com.misaengfly.chordbox.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.misaengfly.chordbox.database.ChordDatabase
import kotlinx.coroutines.runBlocking

class RecordChordViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = ChordDatabase.getInstance(application)

    var chordMap: MutableMap<Int, String> = mutableMapOf()

    fun findRecordItem(filePath: String) {
        runBlocking {
            val record = database.recordDao.getRecord(filePath)

            record?.let {
                val chordList = it.chords.split(" ")
                val timeList = it.times.split(" ")
                var value = 0

                if (chordList[0] != "") {
                    for (i in chordList.indices) {
                        if (chordList[i] == "N") continue

                        value = (timeList[i].toFloat() * 10).toInt()
                        chordMap[value] = chordList[i]
                    }
                }
            }
        }
    }

    /**
     * Factory
     **/
    class Factory(private val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecordChordViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecordChordViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}