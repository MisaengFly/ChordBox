package com.misaengfly.chordbox

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.musiclist.MusicListFragment
import com.misaengfly.chordbox.network.BASE_URL
import com.misaengfly.chordbox.network.FileApi
import com.misaengfly.chordbox.network.RecordResponse
import com.misaengfly.chordbox.network.UrlResponse
import com.misaengfly.chordbox.player.RecordChordFragment
import com.misaengfly.chordbox.player.UrlChordFragment
import com.misaengfly.chordbox.player.UrlChordViewModel
import com.misaengfly.chordbox.record.RecordViewModel
import com.misaengfly.chordbox.record.RecordViewModelFactory
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : AppCompatActivity() {

    private val urlViewModel: UrlChordViewModel by lazy {
        val viewModelFactory = UrlChordViewModel.Factory(application)
        ViewModelProvider(this, viewModelFactory).get(UrlChordViewModel::class.java)
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceUuid(): String? {
        return Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun getCurrentToken() {
        val TAG = "Current Token"
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token.toString())
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // uuid 구하기
        val sharedPreference = getSharedPreferences("SP", MODE_PRIVATE)
        val value = sharedPreference.getString("uuid", null)
        if (value == null) {
            val uuid = getDeviceUuid()
            val editor = sharedPreference.edit()
            uuid.let {
                editor.putString("uuid", uuid)
            }
            editor.commit()
        }

        getCurrentToken()

        val musicListFragment = MusicListFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, musicListFragment)
            .commit()

        // Notification을 눌렀을 때 해당 파일로 이동
        val notifyFile: String? = intent.getStringExtra("Notification")
        notifyFile?.let {
            if (URLUtil.isValidUrl(it)) {
                val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
                val prefToken = pref.getString("token", null)

                saveUrlResultToDB(it, prefToken!!)
            } else {
                val filePath = filesDir.absolutePath.toString() + "/" + it
                moveChordFragment(filePath)
            }
        }
    }

    /**
     * Notification 클릭 시 녹음 파일의 결과값일 때 해당 파일로 이동
     * @param path : 이동할 File Path
     * */
    private fun moveChordFragment(path: String) {
        val fragment = RecordChordFragment()

        val bundle = Bundle()
        bundle.putString("Path", path)
        fragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Notification 클릭 시 DB에 결과 값 저장
     * @param path : 저장할 파일 이름
     * */
    private fun saveUrlResultToDB(url: String, prefToken: String) {
        FileApi.retrofitService.getUrlChord(url, prefToken!!)
            .enqueue(object : Callback<UrlResponse> {
                override fun onResponse(
                    call: Call<UrlResponse>,
                    response: Response<UrlResponse>
                ) {
                    Log.d("Url Chord Download", response.message())
                    response.body()?.let {
                        Log.d("log", it.toString())
                        lifecycleScope.launch {
                            urlViewModel.updateUrlItem(
                                it.chordList,
                                it.timeList,
                                it.url,
                                it.filePath.split("/")[1],
                                it.urlName
                            )
                            downloadUrlFile(it.filePath)
                            moveUrlChordFragment(url)
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
        registerReceiver(onDownloadComplete, intentFilter)

        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val file = File(getExternalFilesDir(null), filePath.split("/")[1])
        val urlPath = "$BASE_URL/download?filename=${filePath.split("/")[1]}"

        val request = DownloadManager.Request(Uri.parse(urlPath))
            .setTitle("Downloading file")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(Uri.fromFile(file))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadId = downloadManager.enqueue(request)
        Log.d("TAG", "path : " + file.path)
    }

    /**
     * Notification 클릭 시 Url 전송의 결과값일 때 해당 파일로 이동
     * */
    private fun moveUrlChordFragment(url: String) {
        val fragment = UrlChordFragment()

        val bundle = Bundle()
        bundle.putString("Url", url)
        fragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_recent_box, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.list_action_alarm -> {
//                true
//                }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}