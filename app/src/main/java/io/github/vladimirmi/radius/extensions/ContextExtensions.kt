package io.github.vladimirmi.radius.extensions

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager


/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

fun Drawable.getBitmapExt(): Bitmap {
    return if (this is BitmapDrawable) {
        this.bitmap
    } else if (this is VectorDrawableCompat || this is VectorDrawable) {
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)

        bitmap
    } else {
        throw IllegalArgumentException("unsupported drawable type")
    }
}

fun Context.getBitmap(@DrawableRes id: Int): Bitmap {
    return ContextCompat.getDrawable(this, id).getBitmapExt()
}

val Context.dp get() = getDisplayMetrics().density.toInt()

val Context.sp get() = getDisplayMetrics().scaledDensity.toInt()

fun Context.getDisplayMetrics(): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    (this.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

val Context.downloadManager: DownloadManager
    get() = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager