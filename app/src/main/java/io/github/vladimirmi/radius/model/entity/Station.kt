package io.github.vladimirmi.radius.model.entity

import android.net.Uri
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.getContentType
import io.github.vladimirmi.radius.extensions.getRedirected
import io.github.vladimirmi.radius.extensions.toURI
import io.github.vladimirmi.radius.extensions.toUrl
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

data class Station(val uri: Uri,
                   val title: String,
                   val group: String = "",
                   val fav: Boolean = false,
                   val id: String = UUID.randomUUID().toString()) {

    val path = Scopes.app.getInstance(Preferences::class.java).appDirPath +
            if (group.isBlank()) "/$title.pls" else "/$group/$title.pls"

    fun toContent(): String {
        return """[playlist]
                        |File1=$uri
                        |Title1=$title
                        |favorite=$fav
                    """.trimMargin()
    }

    companion object {
        fun fromUri(uri: Uri): Station? {
            Timber.e("fromUri: $uri")
            return when {
                uri.scheme.startsWith("http") -> fromNet(uri.toUrl() ?: return null)
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
            //todo icy headers
//            val delimiter = "http://"
//            val urlS = url.toString()
//            val index = urlS.lastIndexOf(delimiter)
//            val newUrl = URL(urlS.substring(index, urlS.length))

            val type = newUrl.getContentType()
            Timber.d("fromNet: url=$newUrl \n type=$type")
            val station = when (ContentTypes.fromString(type)) {
                ContentTypes.PLS -> newUrl.openStream().parsePls()
                null -> null
                else -> newUrl.openStream().parseM3u()
            }
            Timber.d("fromNet: $station")
            return station
        }
    }
}



