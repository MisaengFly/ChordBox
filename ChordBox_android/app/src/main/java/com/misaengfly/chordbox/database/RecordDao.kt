package com.misaengfly.chordbox.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: Record)

    @Update
    suspend fun update(record: Record)

    @Query("DELETE FROM record_table")
    suspend fun clearRecordTable()

    @Query("DELETE FROM record_table WHERE file_absolute_path = :key")
    suspend fun clearRecord(key: String)

    @Query("SELECT * FROM record_table")
    fun getRecords(): LiveData<List<Record>>

    @Query("SELECT * FROM record_table WHERE file_absolute_path = :key")
    suspend fun getRecord(key: String): Record?

    @Query("SELECT * FROM record_table ORDER BY fileName DESC LIMIT 1")
    suspend fun getOne(): Record?
}