package io.github.vladimirmi.internetradioplayer.extensions

import android.app.DownloadManager
import android.net.Uri
import java.net.URI
import java.net.URL

/**
 * Created by Vladimir Mikhalev 09.11.2017.
 */

fun String.toURL(): URL = URL(this.trim())

fun Uri.toURL(): URL = this.toString().toURL()

fun URI.toUri(): Uri = Uri.parse(toString())

fun String.toURI(): URI = URI(this.trim())

fun String.toUri(): Uri = toURI().toUri()



fun DownloadManager.download(from: Uri, to: Uri) {
    val request = DownloadManager.Request(from)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationUri(to)

    enqueue(request)
}
