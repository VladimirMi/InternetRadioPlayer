package io.github.vladimirmi.radius.model.manager

import android.net.Uri
import io.github.vladimirmi.radius.model.entity.Media
import java.io.File
import java.io.PrintWriter

/**
 * Created by Vladimir Mikhalev 01.11.2017.
 */


fun File.parsePls(): Media? {
    var title = name.substringBeforeLast(".")
    var uri: Uri? = null
    var fav = false
    useLines { line ->
        line.forEach {
            when {
                it.startsWith("Title1=") -> title = it.substring(7).trim()
                it.startsWith("File1=") -> uri = Uri.parse(it.substring(6).trim())
                it.startsWith("favorite") -> fav = it.substring(8).trim().toBoolean()
            }
        }
    }
    return uri?.let { Media(title, it, parent, path, fav) }
}

fun File.update(media: Media) {
    val newContent = StringBuilder()
    readText().lines().forEach {
        val line = when {
            it.startsWith("Title1=") -> "Title1=${media.title}\n"
            it.startsWith("File1=") -> "File1=${media.uri}\n"
            it.startsWith("favorite") -> "favorite=${media.fav}\n"
            else -> it
        }
        newContent.append(line)
    }
    clear()
    writeText(newContent.toString())
}

fun File.clear() {
    PrintWriter(this).close()
}