package com.misaengfly.chordbox

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.misaengfly.chordbox.musiclist.MusicListFragment

class MainActivity : AppCompatActivity() {

    private fun getDeviceUuid(): String? {
        return Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
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

        val musicListFragment = MusicListFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, musicListFragment)
            .commit()
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