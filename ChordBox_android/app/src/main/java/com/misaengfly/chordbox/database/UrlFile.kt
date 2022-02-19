package com.misaengfly.chordbox.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.misaengfly.chordbox.MusicType
import com.misaengfly.chordbox.musiclist.MusicItem
import com.misaengfly.chordbox.player.UrlItem

@Entity(tableName = "url_table")
data class UrlFile(
    @PrimaryKey
    var url: String,
    @ColumnInfo(name = "file_absolute_path")
    var fileAbsolutePath: String,
    var fileName: String,
    var duration: String,
    var lastModified: String,
    var chords: String,
    var times: String

) {
    constructor() : this("", "", "", "", "", "", "")
    constructor(url: String, lastModified: String) : this(
        url, "", "", "", lastModified, "", ""
    )
}

fun UrlFile.asUrlItem(): UrlItem {
    val tempMap: MutableMap<Int, String> = mutableMapOf()

    if (!this.chords.isNullOrBlank()) {
        val chordList = this.chords.split(" ")
        val timeList = this.times.split(" ")

        for (i in chordList.indices) {
            if (chordList[i] == "N") continue
            tempMap[(timeList[i].toFloat() * 10).toInt()] = chordList[i]
        }
    }

    return UrlItem(
        type = MusicType.URL,
        url = this.url,
        absolutePath = this.fileAbsolutePath,
        fileName = this.fileName,
        duration = this.duration,
        lastModified = this.lastModified,
        chordMap = tempMap.toMap()
    )
}

fun UrlFile.asDomainModel(): MusicItem {
    val tempMap: MutableMap<Float, String> = mutableMapOf()

    if (!this.chords.isNullOrBlank()) {
        val chordList = this.chords.split(" ")
        val timeList = this.times.split(" ")

        for (i in chordList.indices) {
            tempMap[timeList[i].toFloat()] = chordList[i]
        }
    }

    return MusicItem(
        type = MusicType.URL,
        url = this.url,
        absolutePath = this.fileAbsolutePath,
        fileName = this.fileName,
        duration = this.duration,
        lastModified = this.lastModified,
        chordMap = tempMap.toMap()
    )
}

fun List<UrlFile>.asDomainModel(): List<MusicItem> {
    return map {
        val tempMap: MutableMap<Float, String> = mutableMapOf()

        if (!it.chords.isNullOrBlank()) {
            val chordList = it.chords.split(" ")
            val timeList = it.times.split(" ")

            for (i in chordList.indices) {
                tempMap[timeList[i].toFloat()] = chordList[i]
            }
        }

        MusicItem(
            type = MusicType.URL,
            url = it.url,
            absolutePath = it.fileAbsolutePath,
            fileName = it.fileName,
            duration = it.duration,
            lastModified = it.lastModified,
            chordMap = tempMap.toMap()
        )
    }
}