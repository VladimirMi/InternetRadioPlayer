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
                it.startsWith("favorite=") -> fav = it.substring(9).trim().toBoolean()
            }
        }
    }
    return uri?.let { Media(title, it, parentFile.name, path, fav) }
}

fun Media.savePls() {
    val content = """[playlist]
        |File1=$uri
        |Title1=$title
        |favorite=$fav
    """.trimMargin()

    val file = File(path)
    file.clear()
    file.writeText(content)
}

fun File.clear() {
    PrintWriter(this).close()
}