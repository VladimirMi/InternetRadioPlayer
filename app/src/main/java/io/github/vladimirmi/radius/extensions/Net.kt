package io.github.vladimirmi.radius.extensions

import android.app.DownloadManager
import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

/**
 * Created by Vladimir Mikhalev 09.11.2017.
 */

fun Uri.toUrl(): URL? {
    return try {
        URL(this.toString().trim())
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        null
    }
}

fun Uri.getContentType(): String {
    return this.toUrl()?.getContentType() ?: ""
}

fun URL.getContentType(): String {
    return try {
        val connection = openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        val contentType = connection.contentType.trim().toLowerCase(Locale.ENGLISH)
        connection.disconnect()
        contentType
    } catch (e: IOException) {
        e.printStackTrace()
        ""
    }
}

fun DownloadManager.download(from: Uri, to: Uri) {
    val request = DownloadManager.Request(from)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationUri(to)

    enqueue(request)
}