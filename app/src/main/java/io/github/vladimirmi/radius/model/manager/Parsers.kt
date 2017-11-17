package io.github.vladimirmi.radius.model.manager

import android.net.Uri
import io.github.vladimirmi.radius.extensions.toUri
import io.github.vladimirmi.radius.model.entity.Station
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

fun File.parsePls(group: String) = inputStream().parsePls(name, group)

fun InputStream.parsePls(name: String = "", group: String = ""): Station? {
    var title = name
    var uri: Uri? = null
    var fav = false
    this.bufferedReader().readLines().forEach {
        when {
            it.startsWith("Title1=") -> title = it.substring(7).trim()
            it.startsWith("File1=") -> uri = Uri.parse(it.substring(6).trim())
            it.startsWith("favorite=") -> fav = it.substring(9).trim().toBoolean()
        }
    }
    return uri?.let { Station(it, title, group, fav) }
}


fun File.parseM3u(group: String) = inputStream().parseM3u(name, group)

fun InputStream.parseM3u(name: String = "", group: String = ""): Station? {
    var extended = false
    var title = name
    var uri: Uri? = null

    this.bufferedReader().readLines().forEach {
        when {
            it.startsWith("#EXTM3U") -> extended = true
            extended && it.startsWith("#EXTINF") -> title = it.substringAfter(",")
            else -> uri = try {
                URI(it).toUri()
            } catch (e: URISyntaxException) {
                Timber.e("parseM3u", e)
                null
            }
        }
    }
    return uri?.let { Station(it, title, group) }
}
