package io.github.vladimirmi.internetradioplayer.data.manager

import android.content.Context
import android.net.Uri
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.extensions.toURL
import io.github.vladimirmi.internetradioplayer.extensions.toUri
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

private const val SCHEME_FILE = "file"
private const val SCHEME_CONTENT = "content"
private const val SCHEME_HTTP = "http"

private const val EXT_PLS = "PLS"
private const val EXT_M3U = "M3U"
private const val EXT_M3U8 = "M3U8"
private const val EXT_RAM = "RAM"

private const val HEADER_NAME = "icy-name"
private const val HEADER_GENRE = "icy-genre"
private const val HEADER_URL = "icy-url"
private const val HEADER_BITRATE = "icy-br"
private const val HEADER_SAMPLE = "icy-sr"

private const val PLS_URI = "File1="
private const val PLS_TITLE = "Title1="
private const val M3U_HEADER = "#EXTM3U"
private const val M3U_INFO = "#EXTINF"

private const val PLS_TYPE = "audio/x-scpls"
private val supportedAudioTypes = arrayOf("mpeg", "ogg", "opus", "aac", "aacp")
private val suppotedPlaylists = arrayOf("x-scpls", "mpegurl", "x-mpegurl", "x-mpegURL",
        "vnd.apple.mpegurl", "vnd.apple.mpegurl.audio", "x-pn-realaudio")

private fun MediaType.isAudioStream() = supportedAudioTypes.contains(subtype())

private fun MediaType.isPlaylistFile() = suppotedPlaylists.contains(subtype())

private fun MediaType.isPlsFile() = subtype() == "x-scpls"

class StationParser
@Inject constructor(private val context: Context) {

    fun parseFromUri(uri: Uri): Station {
        return when {
            uri.scheme.startsWith(SCHEME_HTTP) -> parseFromNet(uri) // also https
            uri.scheme == SCHEME_FILE || uri.scheme == SCHEME_CONTENT -> parseFromPlaylistFile(uri)
            else -> throw IllegalArgumentException("Unsupported uri $uri")
        }
    }

    private fun parseFromPlaylistFile(uri: Uri): Station {
        val type = context.contentResolver.getType(uri)
        val name = uri.lastPathSegment ?: ""
        val content = context.contentResolver.openInputStream(uri).use { stream ->
            stream.bufferedReader().use { it.readText() }
        }
        return if (type == PLS_TYPE) content.parsePls(name) else content.parseM3u(name)
    }

    private fun parseFromNet(uri: Uri, name: String = uri.host): Station {
        val client = OkHttpClient()
        val request = Request.Builder().url(uri.toURL()).build()
        val response = client.newCall(request).execute()
        val body = response.body() ?: throw IllegalStateException("Empty body")
        val type = body.contentType() ?: throw IllegalStateException("Empty content type")

        Timber.d("parseFromNet: $type")
        return (if (type.isPlaylistFile()) {
            if (type.isPlsFile()) body.string().parsePls()
            else body.string().parseM3u()

        } else if (type.isAudioStream()) {
            createStation(name, uri, response.headers())

        } else {
            throw IllegalStateException("Unsupported content type $type")

        }).also { body.close() }
    }

    private fun createStation(name: String, uri: Uri, headers: Headers): Station {
        Timber.d("createStation: $headers")

        return Station(
                name = headers[HEADER_NAME] ?: name,
                uri = uri.toString(),
                url = headers[HEADER_URL],
                bitrate = headers[HEADER_BITRATE]?.toInt(),
                sample = headers[HEADER_SAMPLE]?.toInt()).also {
            it.genres = ArrayList(parseGenres(headers[HEADER_GENRE]))
        }
    }

    private fun parseGenres(genres: String?): Set<String> {
        if (genres == null) return emptySet()
        return if (genres.contains(',')) {
            genres.split(',').map { it.trim() }.toSet()
        } else {
            genres.split(' ').map { it.trim() }.toSet()
        }
    }

    private fun File.parsePls(): Station {
        return readText().parsePls(name)
    }

    private fun String.parsePls(name: String = ""): Station {
        var uri: String? = null
        var title: String = name

        lines().forEach {
            val line = it.trim()
            Timber.d("parsePls: $line")
            when {
                line.startsWith(PLS_URI) -> uri = line.substring(PLS_URI.length).trim()
                line.startsWith(PLS_TITLE) -> title = line.substring(PLS_TITLE.length).trim()
            }
        }
        if (uri == null) throw IllegalStateException("Playlist file does not contain stream uri")
        return parseFromNet(uri!!.toUri(), if (title.isBlank()) uri!!.toUri().host else title)
    }


    private fun File.parseM3u() = readText().parseM3u(name)

    private fun String.parseM3u(name: String = ""): Station {
        var extended = false
        var uri: URI? = null
        var title: String = name

        lines().forEach {
            val line = it.trim()
            Timber.d("parseM3u: $line")
            when {
                line.startsWith(M3U_HEADER) -> extended = true
                extended && line.startsWith(M3U_INFO) -> title = line.substringAfter(",")
                else -> uri = try {
                    URI(line)
                } catch (e: URISyntaxException) {
                    null
                }
            }
        }
        if (uri == null) throw IllegalStateException("Playlist file does not contain stream uri")
        return parseFromNet(uri!!.toUri(), if (title.isBlank()) uri!!.toUri().host else title)
    }
}


