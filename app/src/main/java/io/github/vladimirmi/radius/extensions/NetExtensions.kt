package io.github.vladimirmi.radius.extensions

import android.app.DownloadManager
import android.net.Uri
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URI
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

fun Uri.toURI(): URI = URI.create(this.toString())

fun Uri.getContentType(): String = this.toUrl()?.getContentType() ?: ""

fun URI.toUri(): Uri = Uri.parse(this.toString())

fun <T> URL.useConnection(runnable: HttpURLConnection.() -> T?): T? {
    return try {
        val connection = openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        val result = connection.runnable()
        connection.disconnect()
        result
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun URL.getContentType(): String =
        useConnection {
            Timber.d("getContentType: $headerFields")
            contentType.trim().toLowerCase(Locale.ENGLISH)
        } ?: ""

fun URL.getRedirected(): URL {
    return useConnection {
        Timber.d("getRedirected: $headerFields")
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            URL(headerFields["Location"].toString().trim('[', ']'))
        } else {
            this@getRedirected
        }
    } ?: this
}

fun DownloadManager.download(from: Uri, to: Uri) {
    val request = DownloadManager.Request(from)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationUri(to)

    enqueue(request)
}