package com.misaengfly.chordbox.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: Record)

    @Update
    suspend fun update(record: Record)

    @Query("DELETE FROM record_table")
    suspend fun clearRecordTable()

    @Query("SELECT * FROM record_table WHERE file_absolute_path = :key")
    suspend fun getRecord(key: String): Record?

    @Query("SELECT * FROM record_table ORDER BY fileName DESC LIMIT 1")
    suspend fun getOne(): Record?
}