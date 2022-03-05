package com.misaengfly.chordbox.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UrlResponse(
    val url: String,
    val filePath: String,
    val urlName: String,
    val chordList: String,
    val timeList: String
) : Parcelable