package com.misaengfly.chordbox.player

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.*
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.database.UrlFile
import com.misaengfly.chordbox.database.asUrlItem
import com.misaengfly.chordbox.musiclist.MusicListRepository
import com.misaengfly.chordbox.network.BASE_URL
import com.misaengfly.chordbox.network.FileApi
import com.misaengfly.chordbox.network.UrlResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UrlChordViewModel(application: Application, url: String) :
    AndroidViewModel(application) {

    private val database = ChordDatabase.getInstance(application)
    private val musicListRepository = MusicListRepository(database)

    private var _urlItem: MutableLiveData<UrlItem?> =
        musicListRepository.findUrl(url) as MutableLiveData<UrlItem?>
    val urlItem get() = _urlItem

    /**
     * Record_table의 데이터 업데이트
     **/
    suspend fun updateUrlItem(
        chords: String,
        times: String,
        url: String,
        filePath: String,
        urlName: String
    ) = withContext(Dispatchers.IO) {
        musicListRepository.updateUrl(chords, times, url, filePath, urlName)
    }

    fun findUrlItem(url: String, prefToken: String) {
        // TODO ( 로딩 중 화면 보이면서 로딩하도록 )
        if (_urlItem.value?.absolutePath.isNullOrEmpty()) {
            saveUrlResultToDB(url, prefToken)
        }
    }

    /**
     * Notification 클릭 시 DB에 결과 값 저장
     * @param path : 저장할 파일 이름
     * */
    private fun saveUrlResultToDB(url: String, prefToken: String) {
        FileApi.retrofitService.getUrlChord(url, prefToken)
            .enqueue(object : Callback<UrlResponse> {
                override fun onResponse(
                    call: Call<UrlResponse>,
                    response: Response<UrlResponse>
                ) {
                    Log.d("Url Chord Download", response.message())
                    response.body()?.let {
                        Log.d("log", it.toString())
                        viewModelScope.launch {
                            updateUrlItem(
                                it.chordList,
                                it.timeList,
                                it.url,
                                it.filePath.split("/")[1],
                                it.urlName
                            )
                            downloadUrlFile(it.filePath)
                        }
                    }
                }

                override fun onFailure(call: Call<UrlResponse>, t: Throwable) {
                    Log.d("Url Chord Download", t.toString())
                }
            })
    }

    private var downloadId: Long = -1L
    private lateinit var downloadManager: DownloadManager

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                if (downloadId == id) {
                    val query: DownloadManager.Query = DownloadManager.Query()
                    query.setFilterById(id)
                    var cursor = downloadManager.query(query)
                    if (!cursor.moveToFirst()) {
                        return
                    }

                    var columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    var status = cursor.getInt(columnIndex)
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Toast.makeText(context, "Download succeeded", Toast.LENGTH_SHORT).show()
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED == intent.action) {
                Toast.makeText(context, "Notification clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun downloadUrlFile(filePath: String) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
        getApplication<Application>().applicationContext.registerReceiver(
            onDownloadComplete,
            intentFilter
        )

        downloadManager = getApplication<Application>().applicationContext
            .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val file = File(
            getApplication<Application>().applicationContext.getExternalFilesDir(null),
            filePath.split("/")[1]
        )
        val urlPath = "$BASE_URL/download?filename=${filePath.split("/")[1]}"

        val request = DownloadManager.Request(Uri.parse(urlPath))
            .setTitle("Downloading file")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(Uri.fromFile(file))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadId = downloadManager.enqueue(request)
        //Log.d("TAG", "path : " + file.path)
    }

    /**
     * Factory
     **/
    class Factory(private val app: Application, private val url: String) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UrlChordViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UrlChordViewModel(app, url) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}