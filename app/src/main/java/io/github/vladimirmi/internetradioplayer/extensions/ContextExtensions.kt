package io.github.vladimirmi.internetradioplayer.extensions

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat


/**
 * Created by Vladimir Mikhalev 21.10.2017.
 */

fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

val Context.dp get() = getDisplayMetrics().density.toInt()

val Context.sp get() = getDisplayMetrics().scaledDensity.toInt()

fun Context.getDisplayMetrics(): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    (this.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

inline fun Context.startActivitySafe(intent: Intent, onError: () -> Unit = {}) {
    if (packageManager.resolveActivity(intent, 0) != null) {
        startActivity(intent)
    } else {
        onError.invoke()
    }
}

val Context.downloadManager: DownloadManager
    get() = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
