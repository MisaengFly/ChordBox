package com.misaengfly.chordbox.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_table")
data class Record(
    @PrimaryKey
    @ColumnInfo(name = "file_absolute_path")
    var fileAbsolutePath: String,
    var fileName: String,
    var chords: String,
    var times: String
) {
    constructor() : this("", "", "", "")
    constructor(filePath: String, fileName: String) : this(filePath, fileName, "", "")
}
