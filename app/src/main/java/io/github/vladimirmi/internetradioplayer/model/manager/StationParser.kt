package io.github.vladimirmi.internetradioplayer.model.manager

import android.net.Uri
import com.google.gson.Gson
import io.github.vladimirmi.internetradioplayer.extensions.toURL
import io.github.vladimirmi.internetradioplayer.extensions.toUri
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station
import io.github.vladimirmi.internetradioplayer.model.entity.isAudioStream
import io.github.vladimirmi.internetradioplayer.model.entity.isPlaylistFile
import io.github.vladimirmi.internetradioplayer.model.entity.isPlsFile
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
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

    companion object {
        private const val SCHEME_FILE = "file"
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
    }

    @Suppress("TooGenericExceptionCaught")
    fun parseFromJsonFile(file: File): Station? {
        return try {
            gson.fromJson(file.readText(), Station::class.java)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    fun parseFromUri(uri: Uri): Station {
        return when {
            uri.scheme.startsWith(SCHEME_HTTP) -> parseFromNet(uri) // also https
            uri.scheme == SCHEME_FILE -> parseFromPlaylistFile(uri)
            else -> throw IllegalArgumentException("Unsupported uri $uri")
        }
    }

    private fun parseFromPlaylistFile(uri: Uri): Station {
        val file = File(uri.toString())
        if (!file.exists()) {
            throw IllegalStateException("Can not find file for uri $uri")
        }

        return when (file.extension.toUpperCase()) {
            EXT_PLS -> file.parsePls()
            EXT_M3U, EXT_M3U8, EXT_RAM -> file.parseM3u()
            else -> throw IllegalStateException("Unsupported file extension ${file.extension}")
        }
    }

    private fun parseFromNet(uri: Uri, name: String = uri.host): Station {
        val client = OkHttpClient()
        val request = Request.Builder().url(uri.toURL()).build()
        val response = client.newCall(request).execute()
        val body = response.body() ?: throw IllegalStateException("Empty body")
        val type = body.contentType() ?: throw IllegalStateException("Empty content type")

        Timber.d("parseFromNet: $type")
        return (if (type.isPlaylistFile()) {
            if (type.isPlsFile()) body.byteStream().parsePls()
            else body.byteStream().parseM3u()

        } else if (type.isAudioStream()) {
            createStation(response.headers(), name).apply {
                this.uri = uri.toString()
            }

        } else {
            throw IllegalStateException("Unsupported content type $type")

        }).also { body.close() }
    }

    private fun createStation(headers: Headers, name: String): Station {
        Timber.d("createStation: $headers")

        return Station().also {
            it.name = headers[HEADER_NAME] ?: ""
            it.url = headers[HEADER_URL]
            it.bitrate = headers[HEADER_BITRATE]?.toInt()
            it.sample = headers[HEADER_SAMPLE]?.toInt()
            it.genres = parseGenres(headers[HEADER_GENRE])
        }
    }

    private fun parseGenres(genres: String?): List<String> {
        if (genres == null) return emptyList()
        return if (genres.contains(',')) {
            genres.split(',').map { it.trim() }
        } else {
            genres.split(' ').map { it.trim() }
        }
    }

    private fun File.parsePls() = inputStream().parsePls(name)

    private fun InputStream.parsePls(name: String = ""): Station {
        use { inputStream ->
            var uri: String? = null
            var title: String = name

            inputStream.bufferedReader().readLines().forEach {
                val line = it.trim()
                Timber.d("parsePls: $line")
                when {
                    line.startsWith(PLS_URI) -> uri = line.substring(PLS_URI.length).trim()
                    line.startsWith(PLS_TITLE) -> title = line.substring(PLS_TITLE.length).trim()
                }
            }
            return uri?.let {
                parseFromNet(
                        uri = it.toUri(),
                        name = if (title.isBlank()) it.toUri().host else title
                )
            } ?: throw IllegalStateException("Playlist file does not contain stream uri")
        }
    }


    private fun File.parseM3u() = inputStream().parseM3u(name)

    private fun InputStream.parseM3u(name: String = ""): Station {
        use { inputStream ->
            var extended = false
            var uri: URI? = null
            var title: String = name

            inputStream.bufferedReader().readLines().forEach {
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
            return uri?.let {
                parseFromNet(
                        uri = it.toUri(),
                        name = if (title.isBlank()) it.toUri().host else title
                )
            } ?: throw IllegalStateException("Playlist file does not contain stream uri")
        }
    }
}


