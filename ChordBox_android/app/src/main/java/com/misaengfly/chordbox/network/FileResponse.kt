package com.misaengfly.chordbox.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileResponse(
    val fileId: String,
): Parcelable
