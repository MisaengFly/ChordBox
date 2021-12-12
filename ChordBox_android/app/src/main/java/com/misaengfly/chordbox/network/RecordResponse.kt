package com.misaengfly.chordbox.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class RecordResponse(
    val fileName: String,
    val chordList: List<String>,
    val timeList: List<String>
) : Parcelable