package com.misaengfly.chordbox.record

import android.content.Context
import android.graphics.Insets
import android.graphics.Point
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.TimeUnit

const val WAVE_HEADER_SIZE = 44
const val SEEK_OVER_AMOUNT = 5000

val Context.recordFile: File
    get() {
        val fileCount = filesDir.listFiles { _, name ->
            name.contains("musicrecord")
        }?.size ?: 0
        return File(filesDir, "musicrecord${fileCount}.wav")
    }

fun Long.formatAsTime(): String {
    val seconds = (TimeUnit.MILLISECONDS.toSeconds(this) % 60).toInt()
    val minutes = (TimeUnit.MILLISECONDS.toMinutes(this) % 60).toInt()

    return when (val hours = (TimeUnit.MILLISECONDS.toHours(this)).toInt()) {
        0 -> String.format("%02d : %02d", minutes, seconds)
        else -> String.format("%02d : %02d : %02d", hours, minutes, seconds)
    }
}

fun Context.getDrawableCompat(@DrawableRes resId: Int) =
    ContextCompat.getDrawable(this, resId)

internal fun Context.getColorCompat(@ColorRes colorResId: Int): Int =
    ContextCompat.getColor(this, colorResId)

internal fun Context.dpToPx(dp: Float) = dp * resources.displayMetrics.density

fun WindowManager.currentWindowMetricsPointCompat(): Point {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        val windowInsets = currentWindowMetrics.windowInsets
        var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
        windowInsets.displayCutout?.run {
            insets = Insets.max(
                insets,
                Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom)
            )
        }
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom
        Point(
            currentWindowMetrics.bounds.width() - insetsWidth,
            currentWindowMetrics.bounds.height() - insetsHeight
        )
    } else {
        Point().apply {
            defaultDisplay.getSize(this)
        }
    }
}