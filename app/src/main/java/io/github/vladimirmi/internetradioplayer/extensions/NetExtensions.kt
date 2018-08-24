package io.github.vladimirmi.internetradioplayer.extensions

import android.app.DownloadManager
import android.net.Uri
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.*

/**
 * Created by Vladimir Mikhalev 09.11.2017.
 */

fun String.toURL(): URL = URL(this.trim())

fun Uri.toURL(): URL = this.toString().toURL()

fun String.toURI(): URI = URI(this.trim())

fun String.toUri(): Uri = Uri.parse(this.toURI().toString())

fun <T> URL.useConnection(connectTimeout: Int = 5000,
                          readTimeout: Int = 5000,
                          function: HttpURLConnection.() -> T): T {

    val connection = openConnection() as HttpURLConnection

    return try {
        connection.connectTimeout = connectTimeout
        connection.readTimeout = readTimeout
        connection.function()
    } finally {
//        connection.disconnect()  //too costly
    }
}

fun URL.getContentType(): String {
    return useConnection { contentType.trim().toLowerCase(Locale.ENGLISH) }
}

fun URL.getRedirected(): URL {
    val url = useConnection {
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            URL(headerFields["Location"].toString().trim('[', ']'))
        } else {
            this@getRedirected
        }
    }
    return if (url == this) url
    else url.getRedirected()
}

fun DownloadManager.download(from: Uri, to: Uri) {
    val request = DownloadManager.Request(from)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationUri(to)

    enqueue(request)
}
