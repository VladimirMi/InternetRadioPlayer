package io.github.vladimirmi.radius.model.manager

import android.net.Uri
import com.google.gson.Gson
import io.github.vladimirmi.radius.extensions.getContentType
import io.github.vladimirmi.radius.extensions.getRedirected
import io.github.vladimirmi.radius.extensions.toURI
import io.github.vladimirmi.radius.extensions.toURL
import io.github.vladimirmi.radius.model.entity.ContentType
import io.github.vladimirmi.radius.model.entity.Station
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

class StationParser
@Inject constructor(private val gson: Gson) {

    fun parseFromJsonFile(file: File): Station? {
        return try {
            gson.fromJson(file.readText(), Station::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun toJson(station: Station): String {
        return gson.toJson(station)
    }

    fun parseFromUri(uri: Uri): Station? {
        return when {
            uri.scheme.startsWith("http") -> parseFromNet(uri.toURL() ?: return null)
            uri.scheme.startsWith("file") -> parseFromPlaylistFile(File(uri.toURI()))
            else -> null
        }
    }

    private fun parseFromPlaylistFile(file: File): Station? {
        return when (file.extension) {
            "pls" -> file.parsePls()
            "m3u", "m3u8", "ram" -> file.parseM3u()
            else -> null
        }
    }

    private fun parseFromNet(url: URL): Station? {
        val newUrl = url.getRedirected().getRedirected()
        val type = newUrl.getContentType()
        Timber.e("parseFromNet: $type")
        val station = when (ContentType.fromString(type)) {
            ContentType.PLS -> newUrl.openStream().parsePls()
            null -> null
            else -> newUrl.openStream().parseM3u()
        }
        return parseHeaders(station)
    }

    private fun parseHeaders(station: Station?): Station? {
        val headers = station?.uri?.toURL()?.openConnection()?.headerFields ?: return station
        val genresSt = headers["icy-genre"]?.get(0)
        Timber.e("parseHeaders: $genresSt")
        val genres = if (genresSt?.contains(',') == true) {
            genresSt.split(',').map { it.trim() }
        } else {
            genresSt?.split(' ')?.map { it.trim() }
        }
        val copy = station.copy(
                title = headers["icy-name"]?.get(0) ?: station.title,
                genre = genres ?: station.genre,
                url = headers["icy-url"]?.get(0) ?: station.url,
                bitrate = headers["icy-br"]?.get(0)?.toInt() ?: station.bitrate,
                sample = headers["icy-sr"]?.get(0)?.toInt() ?: station.sample)
        Timber.e("parseHeaders: $copy")
        return copy
    }

    private fun File.parsePls() = inputStream().parsePls(name)

    //todo hardcoded strings
    private fun InputStream.parsePls(name: String = ""): Station? {
        var uri: String? = null
        var title: String = name
        var isJson = false
        val json = StringBuilder()
        use {
            this.bufferedReader().readLines().forEach {
                val line = it.trim()
                when {
                    line.startsWith("File1=") -> uri = line.substring(6).trim()
                    line.startsWith("Title1=") -> title = line.substring(7).trim()
                    line.startsWith("/*start json*/") -> isJson = true
                    line.startsWith("/*end json*/") -> isJson = false
                }
                if (isJson) {
                    json.append(it)
                }
            }
            return if (uri != null) {
                if (json.isNotBlank()) {
                    Gson().fromJson(json.toString(), Station::class.java)
                            .copy(uri = uri!!,
                                    title = title)
                } else {
                    Station(uri!!, title)
                }
            } else null
        }
    }


    private fun File.parseM3u() = inputStream().parseM3u(name)

    private fun InputStream.parseM3u(name: String = ""): Station? {
        var extended = false
        var uri: URI? = null
        var title: String = name
        use {
            this.bufferedReader().readLines().forEach {
                val line = it.trim()
                when {
                    line.startsWith("#EXTM3U") -> extended = true
                    extended && line.startsWith("#EXTINF") -> title = line.substringAfter(",")
                    else -> uri = try {
                        URI(line)
                    } catch (e: URISyntaxException) {
                        Timber.e(e)
                        null
                    }
                }
            }
            return uri?.let { Station(it.toString(), title) }
        }
    }

}


