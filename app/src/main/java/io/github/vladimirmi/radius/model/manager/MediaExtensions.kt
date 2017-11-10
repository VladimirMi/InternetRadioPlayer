package io.github.vladimirmi.radius.model.manager

import android.net.Uri
import io.github.vladimirmi.radius.extensions.clear
import io.github.vladimirmi.radius.model.entity.Media
import java.io.File

/**
 * Created by Vladimir Mikhalev 01.11.2017.
 */


fun File.parsePls(): Media? {
    return useLines { it.asIterable().parsePls(this) }
}

fun Iterable<String>.parsePls(file: File): Media? {
    var title = file.name.substringBeforeLast(".")
    var uri: Uri? = null
    var fav = false
    this.forEach {
        when {
            it.startsWith("Title1=") -> title = it.substring(7).trim()
            it.startsWith("File1=") -> uri = Uri.parse(it.substring(6).trim())
            it.startsWith("favorite=") -> fav = it.substring(9).trim().toBoolean()
        }
    }
    return uri?.let { Media(title, it, file.parentFile.name, file.path, fav) }
}

fun Media.saveToPls() {
    val content = """[playlist]
        |File1=$uri
        |Title1=$title
        |favorite=$fav
    """.trimMargin()

    val file = File(path)
    file.clear()
    file.writeText(content)
}