package com.misaengfly.chordbox.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.misaengfly.chordbox.player.UrlItem

@Dao
interface UrlDao {
    @Insert
    suspend fun insert(urlFile: UrlFile)

    @Update
    suspend fun update(urlFile: UrlFile)

    @Query("UPDATE url_table SET chords = :chords, times = :times WHERE url = :url")
    suspend fun updateUrl(chords: String, times: String, url: String)

    @Query("DELETE FROM url_table")
    suspend fun clearUrlTable()

    @Query("DELETE FROM url_table WHERE url = :key")
    suspend fun clearUrl(key: String)

    @Query("SELECT * FROM url_table")
    fun getUrlFiles(): LiveData<List<UrlFile>>

    @Query("SELECT * FROM url_table WHERE url = :key")
    suspend fun getUrlFile(key: String): UrlFile?
}