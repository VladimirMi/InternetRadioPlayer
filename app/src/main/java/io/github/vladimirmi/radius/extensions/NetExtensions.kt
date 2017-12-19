package io.github.vladimirmi.radius.extensions

import android.app.DownloadManager
import android.net.Uri
import java.io.IOException
import java.net.*
import java.util.*

/**
 * Created by Vladimir Mikhalev 09.11.2017.
 */

fun String.toURL(): URL? {
    return try {
        URL(this.trim())
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        null
    }
}

fun Uri.toURL(): URL? = this.toString().toURL()

fun String.toURI(): URI? {
    return try {
        URI(this.trim())
    } catch (e: URISyntaxException) {
        e.printStackTrace()
        null
    }
}

fun Uri.toURI(): URI? = this.toString().toURI()

fun String.toUri(): Uri? = this.toURI()?.toString()?.let { Uri.parse(it) }

fun Uri.getContentType(): String = this.toURL()?.getContentType() ?: ""

fun <T> URL.useConnection(connectTimeout: Int = 5000,
                          readTimeout: Int = 5000,
                          runnable: HttpURLConnection.() -> T?): T? {
    return try {
        val connection = openConnection() as HttpURLConnection
        connection.connectTimeout = connectTimeout
        connection.readTimeout = readTimeout
        val result = connection.runnable()
        connection.disconnect()
        result
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun URL.getContentType(): String {
    return useConnection { contentType.trim().toLowerCase(Locale.ENGLISH) } ?: ""
}

fun URL.getRedirected(): URL {
    return useConnection {
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