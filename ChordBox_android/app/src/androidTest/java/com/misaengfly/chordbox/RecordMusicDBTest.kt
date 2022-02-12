package com.misaengfly.chordbox

import android.graphics.pdf.PdfDocument
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.misaengfly.chordbox.database.ChordDatabase
import com.misaengfly.chordbox.database.Record
import com.misaengfly.chordbox.database.RecordDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RecordMusicDBTest {

    private lateinit var recordDao: RecordDao
    private lateinit var db: ChordDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, ChordDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        recordDao = db.recordDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertRecord() = runBlocking {
        val record = Record(
            InstrumentationRegistry.getInstrumentation().targetContext.filesDir.absolutePath.toString(),
            "record.wav",
            "",
            ""
        )
        recordDao.insert(record)

        recordDao.updateRecord("000", "0000", InstrumentationRegistry.getInstrumentation().targetContext.filesDir.absolutePath.toString())

        val one = recordDao.getOne()
        assertEquals(one?.chords, "000")
    }
}