package io.github.vladimirmi.radius.model.entity

import android.net.Uri
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.toURI
import io.github.vladimirmi.radius.extensions.toUrl
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.manager.parsePls
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
            if (group.isBlank()) "/$title" else "/$group/$title"


    companion object {
        fun fromUri(uri: Uri): Station {
            return when {
                uri.scheme.startsWith("http") -> fromNet(uri.toUrl()
                        ?: throw  IllegalStateException("$uri to url convertation error"))

                uri.scheme.startsWith("file") -> fromFile(File(uri.toURI()))

                else -> throw  IllegalStateException("${uri.scheme} doesn't supported")
            }
        }

        fun fromFile(file: File): Station {
            return when (file.extension) {
                "pls" -> file.parsePls()
                else -> throw  IllegalStateException("${file.extension} doesn't supported")
            }
        }

        private fun fromNet(url: URL): Station {
            return url.openStream().parsePls()
        }
    }
}



