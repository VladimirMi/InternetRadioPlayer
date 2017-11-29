package io.github.vladimirmi.radius.extensions

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.WindowManager
import io.github.vladimirmi.radius.R


/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

fun Context.getBitmap(@DrawableRes id: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, id)

    return if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable) {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        bitmap
    } else {
        throw IllegalArgumentException("unsupported drawable type")
    }
}

//todo wtf
fun Context.getIconTextColors(char: Char): Pair<Int, Int> {
    val textColors = resources.getIntArray(R.array.icon_text_color_set)
    val bgColors = resources.getIntArray(R.array.icon_bg_color_set)
    val colorIdx = char.toInt() % textColors.size
    return Pair(textColors[colorIdx], bgColors[colorIdx])
}

//todo int.dp
val Context.dp get() = getDisplayMetrics().density.toInt()

val Context.sp get() = getDisplayMetrics().scaledDensity.toInt()

fun Context.getDisplayMetrics(): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    (this.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

val Context.downloadManager: DownloadManager
    get() = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager