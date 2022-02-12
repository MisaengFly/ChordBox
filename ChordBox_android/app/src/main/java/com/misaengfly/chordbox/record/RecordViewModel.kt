package com.misaengfly.chordbox.record

import android.app.Application
import android.media.MediaMetadataRetriever
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.misaengfly.chordbox.database.Record
import com.misaengfly.chordbox.database.RecordDao
import com.misaengfly.chordbox.network.RecordResponse
import kotlinx.coroutines.launch
import java.io.File

class RecordViewModel(val database: RecordDao, application: Application) :
    AndroidViewModel(application) {

    /**
     * Record_table에 데이터 삽입
     **/
    fun insertRecord(filePath: String, fileName: String) {
        viewModelScope.launch {
            val file = File(filePath)
            val duration = getFileDuration(file)
            val record = Record(
                filePath, fileName, duration, file.lastModified().convertLongToDateTime()
            )
            database.insert(record)
        }
    }

    /**
     * Record_table의 데이터 업데이트
     **/
    fun updateRecord(chords: String, times: String, filePath: String) {
        viewModelScope.launch {
            database.updateRecord(chords, times, filePath)
        }
    }

    /**
     * 파일 재생 시간 구하기
     * */
    private fun getFileDuration(file: File): String {
        var durationString = ""

        val retriever = MediaMetadataRetriever()
        file.absolutePath.let {
            retriever.setDataSource(file.absolutePath)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInMilliSec = time!!.toLong()
            val duration = timeInMilliSec / 1000
            val hours = duration / 3600
            val minutes = (duration - hours * 3600) / 60
            val seconds = duration - (hours * 3600 + minutes * 60)

            if (hours > 0) {
                durationString += hours.toInt()
                durationString += ":"
            }
            durationString += "${minutes.toInt()}:${seconds.toInt()}"
        }
        return durationString
    }
}