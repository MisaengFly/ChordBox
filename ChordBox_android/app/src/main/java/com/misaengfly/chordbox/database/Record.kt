package com.misaengfly.chordbox.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.misaengfly.chordbox.MusicType
import com.misaengfly.chordbox.musiclist.MusicItem

@Entity(tableName = "record_table")
data class Record(
    @PrimaryKey
    @ColumnInfo(name = "file_absolute_path")
    var fileAbsolutePath: String,
    var fileName: String,
    var duration: String,
    var lastModified: String,
    var chords: String,
    var times: String
) {
    constructor() : this("", "", "", "", "", "")
    constructor(filePath: String, fileName: String, duration: String, lastModified: String) : this(
        filePath, fileName, duration, lastModified, "", ""
    )
}

fun List<Record>.asDomainModel(): List<MusicItem> {
    return map {
        val tempMap: MutableMap<Int, String> = mutableMapOf()

        if (!it.chords.isNullOrBlank()) {
            val chordList = it.chords.split(" ")
            val timeList = it.times.split(" ")

            for (i in chordList.indices) {
                tempMap[timeList[i].toInt()] = chordList[i]
            }
        }

        MusicItem(
            type = MusicType.RECORD,
            url = "",
            absolutePath = it.fileAbsolutePath,
            fileName = it.fileName,
            duration = it.duration,
            lastModified = it.lastModified,
            chordMap = tempMap.toMap()
        )
    }
}