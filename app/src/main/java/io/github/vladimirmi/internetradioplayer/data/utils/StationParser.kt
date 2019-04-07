package io.github.vladimirmi.internetradioplayer.data.utils

import android.content.Context
import android.net.Uri
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.extensions.toURL
import okhttp3.*
import timber.log.Timber
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

const val SCHEME_HTTP = "http"
private const val SCHEME_FILE = "file"
private const val SCHEME_CONTENT = "content"

private const val EXT_PLS = "pls"
private const val EXT_M3U = "m3u"
private const val EXT_M3U8 = "m3u8"
private const val EXT_RAM = "ram"

private const val HEADER_NAME = "icy-name"
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

private val MediaType.encoding: String
    get() = when (subtype()) {
        "ogg", "opus" -> "OGG"
        "aac", "aacp" -> "AAC"
        else -> "MP3"
    }

private fun MediaType.isPlaylistFile() = suppotedPlaylists.contains(subtype())

private fun MediaType.isPlsFile() = subtype() == "x-scpls"

class StationParser
@Inject constructor(private val context: Context,
                    private val client: OkHttpClient,
                    private val networkChecker: NetworkChecker) {

    fun parseFromStation(station: Station): Station {
        val newStation = parseFromNet(station.uri.toURL(), station.name)

        //todo try use MediaExtractor
        return station.copy(
                bitrate = newStation.bitrate ?: station.bitrate,
                encoding = newStation.encoding ?: station.encoding,
                sample = newStation.sample ?: station.sample,
                url = newStation.url ?: station.url
        )
    }

    fun parseFromUri(uri: Uri, name: String?): Station {
        Timber.d("parseFromUri: $uri")
        return when {
            uri.scheme?.startsWith(SCHEME_HTTP, true) == true -> parseFromNet(uri.toURL(), name) // also https
            uri.scheme == SCHEME_FILE || uri.scheme == SCHEME_CONTENT -> parseFromPlaylistFile(uri)
            else -> throw IllegalArgumentException("Error: Unsupported uri $uri")
        }
    }

    private fun parseFromPlaylistFile(uri: Uri): Station {
        val type = context.contentResolver.getType(uri)
        val name = uri.lastPathSegment ?: ""
        val content = context.contentResolver.openInputStream(uri).use { stream: InputStream ->
            stream.bufferedReader().use { it.readText() }
        }
        return if (type == PLS_TYPE
                || type == null && name.substringAfterLast('.').toLowerCase() == EXT_PLS) {
            content.parsePls()
        } else content.parseM3u()
    }

    private fun parseFromNet(url: URL, name: String?): Station {
        if (!networkChecker.isAvailable()) throw IllegalStateException("Error: No connection")

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val code = response.code()
        Timber.d("parseFromNet: $response")

        if (code !in IntRange(200, 299)) throw IllegalStateException("${response.message()} : $code")
        val body = response.body() ?: throw IllegalStateException("Error: Empty body")
        val type = body.contentType() ?: throw IllegalStateException("Error: Empty content type")

        Timber.d("parseFromNet: $type")
        return (if (type.isPlaylistFile()) {
            if (type.isPlsFile()) body.string().parsePls()
            else body.string().parseM3u()

        } else if (type.isAudioStream()) {
            val finalUrl = response.request().url()
            val originUrl = HttpUrl.get(url) ?: finalUrl
            createStation(name, originUrl, finalUrl, response.headers(), type.encoding)

        } else {
            throw IllegalStateException("Error: Unsupported content type $type")

        }).also { body.close() }
    }

    private fun createStation(
            name: String?,
            originUrl: HttpUrl,
            finalUrl: HttpUrl,
            headers: Headers,
            encoding: String
    ): Station {
        Timber.d("createStation: $headers")

        return Station(
                name = headers[HEADER_NAME] ?: name ?: finalUrl.host(),
                uri = originUrl.toString(),
                url = headers[HEADER_URL],
                encoding = encoding,
                bitrate = headers[HEADER_BITRATE],
                sample = headers[HEADER_SAMPLE],
                remoteId = ""
        )
    }

    private fun String.parsePls(): Station {
        var url: URL? = null
        var title: String? = null

        for (l in lines()) {
            val line = l.trim()
            if (line.isEmpty()) continue
            Timber.d("parsePls: $line")
            when {
                line.startsWith(PLS_URI, true) -> url = line.substring(PLS_URI.length).trim().toURL()
                line.startsWith(PLS_TITLE, true) -> title = line.substring(PLS_TITLE.length).trim()
            }
        }
        if (url == null) throw IllegalStateException("Error: Playlist file does not contain stream uri")
        if (title == null) {
            title = url.host ?: url.toString()
        }
        return parseFromNet(url, title)
    }

    private fun String.parseM3u(): Station {
        var extended = false
        var url: URL? = null
        var title: String? = null

        for (l in lines()) {
            val line = l.trim()
            if (line.isEmpty()) continue
            Timber.d("parseM3u: $line")
            when {
                line.startsWith(M3U_HEADER, true) -> extended = true
                extended && line.startsWith(M3U_INFO, true) -> title = line.substringAfter(",")
                !line.startsWith("#EXT", true) -> url = try {
                    URL(line)
                } catch (e: MalformedURLException) {
                    Timber.w(e)
                    url
                }
            }
        }
        if (url == null) throw IllegalStateException("Error: Playlist file does not contain stream uri")
        if (title == null) {
            title = url.host ?: url.toString()
        }
        return parseFromNet(url, title)
    }
}


