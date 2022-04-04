package com.misaengfly.chordbox.dialog

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.database.UrlFile
import com.misaengfly.chordbox.musiclist.MusicItem
import com.misaengfly.chordbox.musiclist.MusicListRepository
import com.misaengfly.chordbox.network.FileApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SendUrlBottomViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = ChordDatabase.getInstance(application)
    private val musicListRepository = MusicListRepository(database)

    /**
     * DB에 저장
     * */
    fun insertUrlToDB(url: String) {
        val date = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val curTime = dateFormat.format(date)

        viewModelScope.launch {
            val urlInfo = UrlFile(
                url,
                curTime
            )
            musicListRepository.insertUrl(urlInfo)
        }
    }

    /**
     * 서버로 url 전송
     **/
    fun sendUrlToServer(url: String, prefToken: String) {
        viewModelScope.launch {
            FileApi.retrofitService.sendYoutubeUrl(url, prefToken)
                .enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        Log.d("Send URL cb success : ", response.message())
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.d("Send URL cb failure", t.toString())
                    }
                })
        }
    }

    fun isExistUrl(url: String): Boolean {
        return runBlocking {
            val musicItem = musicListRepository.isExistUrl(url)
            musicItem != null
        }
    }

    /**
     * Factory
     **/
    class Factory(private val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SendUrlBottomViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SendUrlBottomViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}