package com.misaengfly.chordbox.record

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.misaengfly.chordbox.database.Record
import com.misaengfly.chordbox.database.RecordDao
import kotlinx.coroutines.launch

class RecordViewModel(val database: RecordDao, application: Application) :
    AndroidViewModel(application) {

    /**
     * Record_table에 데이터 삽입
     **/
    fun insertRecord(filePath: String, fileName: String) {
        viewModelScope.launch {
            val record = Record(filePath, fileName)
            database.insert(record)
        }
    }

    fun updateRecord() {
        viewModelScope.launch {

        }
    }

}