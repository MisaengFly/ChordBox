package com.misaengfly.chordbox

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.misaengfly.chordbox.musiclist.MusicListFragment
import com.misaengfly.chordbox.network.FileApi
import com.misaengfly.chordbox.network.RecordResponse
import com.misaengfly.chordbox.player.ChordFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

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
            saveResultToDB(it)
            val path = filesDir.absolutePath.toString() + "/" + it
            moveChordFragment(path)
        }
    }

    /**
     * Notification 클릭 시 DB에 결과 값 저장
     * @param path : 저장할 파일 이름
     * */
    private fun saveResultToDB(fileName: String) {
        FileApi.retrofitService.getRecordChord(fileName).enqueue(object : Callback<RecordResponse> {
            override fun onResponse(
                call: Call<RecordResponse>,
                response: Response<RecordResponse>
            ) {
                Log.d("Record Chord Download", response.message())
            }

            override fun onFailure(call: Call<RecordResponse>, t: Throwable) {
                Log.d("Record Chord Download", t.toString())
            }
        })
    }

    /**
     * Notification 클릭 시 해당 파일로 이동
     * @param path : 이동할 File Path
     * */
    private fun moveChordFragment(path: String) {
        val fragment = ChordFragment()

        val bundle = Bundle()
        bundle.putString("Path", path)
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