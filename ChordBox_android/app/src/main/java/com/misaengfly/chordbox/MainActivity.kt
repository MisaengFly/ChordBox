package com.misaengfly.chordbox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.misaengfly.chordbox.musiclist.MusicListFragment
import com.misaengfly.chordbox.record.RecordActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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