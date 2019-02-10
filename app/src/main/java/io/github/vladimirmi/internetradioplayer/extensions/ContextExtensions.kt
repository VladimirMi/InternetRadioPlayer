package io.github.vladimirmi.internetradioplayer.extensions

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.wifi.WifiManager
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
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

inline fun Context.startActivitySafe(intent: Intent, onError: () -> Unit = {}) {
    if (packageManager.resolveActivity(intent, 0) != null) {
        startActivity(intent)
    } else {
        onError.invoke()
    }
}

fun Context.getScreenSize(): Pair<Int, Int> {
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size.x to size.y
}

val Context.downloadManager: DownloadManager
    get() = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

val Context.windowManager: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

val Context.wifiManager: WifiManager
    get() = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
