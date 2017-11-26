package io.github.vladimirmi.radius.model.manager

import com.google.gson.Gson
import io.github.vladimirmi.radius.model.entity.Station
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

fun File.parsePls(group: String) = inputStream().parsePls(name, group)

//todo hardcoded strings
fun InputStream.parsePls(name: String = "", group: String = ""): Station? {
    var uri: String? = null
    var title: String = name
    var isJson = false
    val json = StringBuilder()
    use {
        this.bufferedReader().readLines().forEach {
            val line = it.trim()
            when {
                line.startsWith("File1=") -> uri = line.substring(6).trim()
                line.startsWith("Title1=") -> title = line.substring(7).trim()
                line.startsWith("/*start json*/") -> isJson = true
                line.startsWith("/*end json*/") -> isJson = false
            }
            if (isJson) {
                json.append(it)
            }
        }
        return if (uri != null) {
            if (json.isNotBlank()) {
                Gson().fromJson(json.toString(), Station::class.java)
                        .copy(uri = uri!!,
                                title = title,
                                group = group)
            } else {
                Station(uri!!, title, group)
            }
        } else null
    }
}


fun File.parseM3u(group: String) = inputStream().parseM3u(name, group)

fun InputStream.parseM3u(name: String = "", group: String = ""): Station? {
    var extended = false
    var uri: URI? = null
    var title: String = name
    use {
        this.bufferedReader().readLines().forEach {
            val line = it.trim()
            when {
                line.startsWith("#EXTM3U") -> extended = true
                extended && line.startsWith("#EXTINF") -> title = line.substringAfter(",")
                else -> uri = try {
                    URI(line)
                } catch (e: URISyntaxException) {
                    Timber.e(e)
                    null
                }
            }
        }
        return uri?.let { Station(it.toString(), title, group) }
    }
}
