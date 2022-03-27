package com.misaengfly.chordbox.player

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.network.FileApi
import com.misaengfly.chordbox.network.RecordResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.MutableMap
import kotlin.collections.indices
import kotlin.collections.mutableMapOf
import kotlin.collections.set

class RecordChordViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = ChordDatabase.getInstance(application)

    var uuid: String = ""
    var token: String = ""

    var chordMap: MutableMap<Int, String> = mutableMapOf()

    private fun updateRecordItem(filePath: String, fileName: String) {
        val sendFileName = (uuid + "_" + fileName)

        FileApi.retrofitService.getRecordChord(sendFileName, token!!)
            .enqueue(object : Callback<RecordResponse> {
                override fun onResponse(
                    call: Call<RecordResponse>,
                    response: Response<RecordResponse>
                ) {
                    Log.d("Record Chord Download", response.message())
                    response.body()?.let {
                        viewModelScope.launch {
                            database.recordDao.updateRecord(it.chordList, it.timeList, filePath)
                        }
                    }
                }

                override fun onFailure(call: Call<RecordResponse>, t: Throwable) {
                    Log.d("Record Chord Download", t.toString())
                }
            })
    }

    fun findRecordItem(filePath: String, fileName: String) {
        runBlocking {
            val record = database.recordDao.getRecord(filePath)

            // TODO ( update 되자마자 바로 View에 반영할 수 있도록 수정 )
            if (record?.chords == "") {
                updateRecordItem(filePath, fileName)
            }

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