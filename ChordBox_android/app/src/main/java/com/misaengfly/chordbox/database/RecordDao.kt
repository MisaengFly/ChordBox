package com.misaengfly.chordbox.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: Record)

    @Update
    suspend fun update(record: Record)

    @Query("UPDATE record_table SET chords = :chords, times = :times WHERE file_absolute_path = :path")
    suspend fun updateRecord(chords: String, times: String, path: String)

    @Query("DELETE FROM record_table")
    suspend fun clearRecordTable()

    @Query("DELETE FROM record_table WHERE file_absolute_path = :key")
    suspend fun clearRecord(key: String)

    @Query("SELECT * FROM record_table")
    fun getRecords(): LiveData<List<Record>>

    @Query("SELECT * FROM record_table WHERE file_absolute_path = :key")
    fun getRecord(key: String): LiveData<Record?>

    @Query("SELECT * FROM record_table ORDER BY fileName DESC LIMIT 1")
    suspend fun getOne(): Record?
}