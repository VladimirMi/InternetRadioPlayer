package io.github.vladimirmi.radius.extensions

import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

/**
 * Created by Vladimir Mikhalev 09.11.2017.
 */

fun Uri.getContentType(): String {
    return try {
        URL(this.toString()).getContentType()
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        ""
    }
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