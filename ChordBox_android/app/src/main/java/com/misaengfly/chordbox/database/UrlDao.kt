package com.misaengfly.chordbox.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UrlDao {
    @Insert
    suspend fun insert(urlFile: UrlFile)

    @Update
    suspend fun update(urlFile: UrlFile)

    @Query("DELETE FROM url_table")
    suspend fun clearUrlTable()

    @Query("SELECT * FROM url_table")
    fun getUrlFiles(): LiveData<List<UrlFile>>

    @Query("SELECT * FROM url_table WHERE file_absolute_path = :key")
    suspend fun getUrlFile(key: String): UrlFile?
}