package io.github.vladimirmi.radius.model.entity

import android.net.Uri
import com.google.gson.GsonBuilder
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.getContentType
import io.github.vladimirmi.radius.extensions.getRedirected
import io.github.vladimirmi.radius.extensions.toURI
import io.github.vladimirmi.radius.extensions.toURL
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.manager.parseM3u
import io.github.vladimirmi.radius.model.manager.parsePls
import timber.log.Timber
import java.io.File
import java.net.URL
import java.util.*


/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

data class Station(val uri: String,
                   val title: String,
                   val group: String,
                   val genre: List<String> = emptyList(),
                   val url: String = "",
                   val bitrate: Int = 0,
                   val sample: Int = 0,
                   val favorite: Boolean = false,
                   val id: String = UUID.randomUUID().toString()) {


    val path = Scopes.app.getInstance(Preferences::class.java).appDirPath +
            if (group.isBlank()) "/$title.pls" else "/$group/$title.pls"

    fun toContent(): String {
        Timber.e("toContent: $this")
        return """[playlist]
                        |File1=$uri
                        |Title1=$title
                        |
                        |/*start json*/
                        |${GsonBuilder().setPrettyPrinting().create().toJson(this)}
                        |/*end json*/
                    """.trimMargin()
    }

    companion object {
        fun fromUri(uri: Uri): Station? {
            return when {
                uri.scheme.startsWith("http") -> fromNet(uri.toURL() ?: return null)
                uri.scheme.startsWith("file") -> fromFile(File(uri.toURI()), true)
                else -> null
            }
        }

        fun fromFile(file: File, import: Boolean = false): Station? {
            val group = if (import || file.parentFile.path == Scopes.app.getInstance(Preferences::class.java).appDirPath) {
                ""
            } else file.parentFile.name
            return when (file.extension) {
                "pls" -> file.parsePls(group)
                "m3u", "m3u8", "ram" -> file.parseM3u(group)
                else -> null
            }
        }

        private fun fromNet(url: URL): Station? {
            val newUrl = url.getRedirected().getRedirected()

            val type = newUrl.getContentType()
            val station = when (ContentTypes.fromString(type)) {
                ContentTypes.PLS -> newUrl.openStream().parsePls()
                null -> null
                else -> newUrl.openStream().parseM3u()
            }
            return parseHeaders(station)
        }

        private fun parseHeaders(station: Station?): Station? {
            val headers = station?.uri?.toURL()?.openConnection()?.headerFields ?: return station
            val genresSt = headers["icy-genre"]?.get(0)
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
    }
}



