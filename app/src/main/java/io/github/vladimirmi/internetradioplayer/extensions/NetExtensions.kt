package io.github.vladimirmi.internetradioplayer.extensions

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

fun <T> URL.useConnection(connectTimeout: Int = 3000,
                          readTimeout: Int = 3000,
                          function: HttpURLConnection.() -> T?): T? {
    val connection = try {
        openConnection() as HttpURLConnection
    } catch (e: IOException) {
        return null
    }
    return try {
        connection.connectTimeout = connectTimeout
        connection.readTimeout = readTimeout
        connection.function()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } finally {
//        connection.disconnect()  //too costly
    }
}

fun URL.getContentType(): String {
    return useConnection { contentType.trim().toLowerCase(Locale.ENGLISH) } ?: ""
}

fun URL.getRedirected(): URL {
    val url = useConnection {
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            URL(headerFields["Location"].toString().trim('[', ']'))
        } else {
            this@getRedirected
        }
    } ?: this
    return if (url == this) url
    else url.getRedirected()
}

fun DownloadManager.download(from: Uri, to: Uri) {
    val request = DownloadManager.Request(from)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationUri(to)

    enqueue(request)
}