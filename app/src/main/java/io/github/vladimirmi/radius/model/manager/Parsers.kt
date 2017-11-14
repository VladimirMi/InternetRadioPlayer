package io.github.vladimirmi.radius.model.manager

import android.net.Uri
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.entity.Station
import java.io.File
import java.io.InputStream

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

fun File.parsePls(): Station? {
    val group = if (parentFile.path == Scopes.app.getInstance(Preferences::class.java).appDirPath) ""
    else parentFile.name
    return inputStream().parsePls(name, group)
}

fun InputStream.parsePls(name: String = "default", group: String = ""): Station? {
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

