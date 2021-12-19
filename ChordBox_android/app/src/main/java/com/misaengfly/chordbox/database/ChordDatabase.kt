package com.misaengfly.chordbox.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Record::class, UrlFile::class], version = 1, exportSchema = false)
abstract class ChordDatabase : RoomDatabase() {
    abstract val recordDao: RecordDao
    abstract val urlDao: UrlDao

    companion object {
        @Volatile
        private var INSTANCE: ChordDatabase? = null

        fun getInstance(context: Context): ChordDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ChordDatabase::class.java,
                        "chord_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}