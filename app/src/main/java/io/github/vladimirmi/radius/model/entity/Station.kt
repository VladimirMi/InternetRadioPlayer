package io.github.vladimirmi.radius.model.entity

import android.net.Uri
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.toURI
import io.github.vladimirmi.radius.extensions.toUrl
import io.github.vladimirmi.radius.model.manager.Preferences
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
            Timber.e("fromUri: ${Thread.currentThread().name}")
            return when {
                uri.scheme.startsWith("http") -> fromNet(uri.toUrl() ?: return null)
                uri.scheme.startsWith("file") -> fromFile(File(uri.toURI()))
                else -> null
            }
        }

        fun fromFile(file: File): Station? {
            return when (file.extension) {
                "pls" -> file.parsePls()
                else -> null
            }
        }

        private fun fromNet(url: URL): Station? = url.openStream().parsePls()
    }
}



