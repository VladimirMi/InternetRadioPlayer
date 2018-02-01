package io.github.vladimirmi.internetradioplayer.model.manager

import android.net.Uri
import com.google.gson.Gson
import io.github.vladimirmi.internetradioplayer.extensions.getContentType
import io.github.vladimirmi.internetradioplayer.extensions.getRedirected
import io.github.vladimirmi.internetradioplayer.extensions.toURL
import io.github.vladimirmi.internetradioplayer.extensions.useConnection
import io.github.vladimirmi.internetradioplayer.model.entity.ContentType
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.net.URI
import java.net.URISyntaxException
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
            Timber.e(e)
            null
        }
    }

    fun toJson(station: Station): String {
        return gson.toJson(station)
    }

    fun parseFromUri(uri: Uri): Station {
        return when {
            uri.scheme.startsWith("http") -> parseFromNet(uri)
            uri.scheme.startsWith("file") -> parseFromPlaylistFile(uri)
            else -> throw IllegalArgumentException("Unsupported uri $uri")
        }
    }

    private fun parseFromPlaylistFile(uri: Uri): Station {
        val file = File(uri.toString())
        if (!file.exists()) {
            throw IllegalStateException("Can not find file for uri $uri")
        }

        return when (file.extension) {
            "pls" -> file.parsePls()
            "m3u", "m3u8", "ram" -> file.parseM3u()
            else -> throw IllegalStateException("Unsupported file extension ${file.extension}")
        }
    }

    private fun parseFromNet(uri: Uri): Station {
        val url = uri.toURL()

        val newUrl = url.getRedirected()
        val type = newUrl.getContentType()
        val station: Station = when {
            ContentType.isAudio(type) -> Station(name = url.host, uri = url.toString())
            ContentType.isPlaylist(type) -> {
                when (ContentType.fromString(type)) {
                    ContentType.PLS -> newUrl.openStream().parsePls()
                    else -> newUrl.openStream().parseM3u()
                }
            }
            else -> throw IllegalStateException("Unsupported content type $type")
        }
        return parseHeaders(station)
    }

    private fun parseHeaders(station: Station): Station {
        return station.uri.toURL().useConnection {
            Timber.d("parseHeaders: $headerFields")
            val title = headerFields["icy-name"]?.get(0)
            val genres = parseGenres(headerFields["icy-genre"]?.get(0))
            val url = headerFields["icy-url"]?.get(0)
            val bitrate = headerFields["icy-br"]?.get(0)?.toInt()
            val sample = headerFields["icy-sr"]?.get(0)?.toInt()

            station.copy(
                    name = title ?: station.name,
                    genre = genres ?: station.genre,
                    url = url ?: station.url,
                    bitrate = bitrate ?: station.bitrate,
                    sample = sample ?: station.sample)
        }
    }

    private fun parseGenres(genres: String?): List<String>? {
        if (genres == null) return null
        return if (genres.contains(',')) {
            genres.split(',').map { it.trim() }
        } else {
            genres.split(' ').map { it.trim() }
        }
    }

    private fun File.parsePls() = inputStream().parsePls(name)

    private fun InputStream.parsePls(name: String = ""): Station {
        var uri: String? = null
        var title: String = name
        use {
            this.bufferedReader().readLines().forEach {
                val line = it.trim()
                when {
                    line.startsWith("File1=") -> uri = line.substring(6).trim()
                    line.startsWith("Title1=") -> title = line.substring(7).trim()
                }
            }
            return uri?.let { Station(it, title) }
                    ?: throw IllegalStateException("Playlist file does not contain stream uri")
        }
    }


    private fun File.parseM3u() = inputStream().parseM3u(name)

    private fun InputStream.parseM3u(name: String = ""): Station {
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
                        null
                    }
                }
            }
            return uri?.let { Station(it.toString(), title) }
                    ?: throw IllegalStateException("Playlist file does not contain stream uri")
        }
    }
}


