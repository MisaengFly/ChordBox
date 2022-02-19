package com.misaengfly.chordbox

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.musiclist.MusicListFragment
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

    private val viewModel: RecordViewModel by lazy {
        val dataSource = ChordDatabase.getInstance(this.application).recordDao
        val viewModelFactory = RecordViewModelFactory(dataSource, this.application)
        ViewModelProvider(this, viewModelFactory).get(RecordViewModel::class.java)
    }

    private val urlViewModel: UrlChordViewModel by lazy {
        val viewModelFactory = UrlChordViewModel.Factory(application)
        ViewModelProvider(this, viewModelFactory).get(UrlChordViewModel::class.java)
    }

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
                saveResultToDB(it)
            }
        }
    }

    /**
     * Notification 클릭 시 DB에 결과 값 저장
     * @param path : 저장할 파일 이름
     * */
    private fun saveResultToDB(fileName: String) {
        val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        val prefToken = pref.getString("token", null)

        val sharedPreference = getSharedPreferences("SP", MODE_PRIVATE)
        val value = sharedPreference.getString("uuid", null)
        val sendFileName = (value + "_" + fileName)

        val filePath = filesDir.absolutePath.toString() + "/" + fileName

        FileApi.retrofitService.getRecordChord(sendFileName, prefToken!!)
            .enqueue(object : Callback<RecordResponse> {
                override fun onResponse(
                    call: Call<RecordResponse>,
                    response: Response<RecordResponse>
                ) {
                    Log.d("Record Chord Download", response.message())
                    response.body()?.let {
                        lifecycleScope.launch {
                            viewModel.updateRecord(it.chordList, it.timeList, filePath)
                            moveChordFragment(filePath)
                        }
                    }
                }

                override fun onFailure(call: Call<RecordResponse>, t: Throwable) {
                    Log.d("Record Chord Download", t.toString())
                }
            })
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
                                it.url
                            )
                            moveUrlChordFragment(url)
                        }
                    }
                }

                override fun onFailure(call: Call<UrlResponse>, t: Throwable) {
                    Log.d("Url Chord Download", t.toString())
                }
            })
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_recent_box, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.list_action_alarm -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}