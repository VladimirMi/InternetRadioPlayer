package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.net.Uri
import android.os.Environment
import io.github.vladimirmi.radius.extensions.clear
import io.github.vladimirmi.radius.extensions.toUrl
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.Preferences
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.run
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.net.URL
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationSource
@Inject constructor(private val context: Context,
                    private val preferences: Preferences) {

    private val appDir: File by lazy { initAppDir() }

    fun getStationList(): ArrayList<Station> {
        copyFilesFromAssets()
        val stationList = ArrayList<Station>()
        val treeWalk = initAppDir().walkTopDown()
        treeWalk.forEach {
            if (!it.isDirectory) {
                Timber.e("file ${it.path}")
                fromFile(it)?.let { stationList.add(it) }
            }
        }
        return stationList
    }

    fun save(station: Station) {
        with(station) {
            val content =
                    """[playlist]
                        |File1=$uri
                        |Title1=$title
                        |favorite=$fav
                    """.trimMargin()

            val file = File(path)
            file.clear()
            file.writeText(content)
        }
    }

    fun clear(station: Station) {
        File(station.path).clear()
    }

    fun fromUri(uri: Uri, cb: (Station?) -> Unit) {
        when {
            uri.scheme.startsWith("http") -> fromNet(uri.toUrl() ?: return, cb)
            uri.scheme.startsWith("file") -> fromFile(File(URI.create(uri.toString())), cb)
        }
    }

    private fun copyFilesFromAssets() {
        if (preferences.firstRun) {
            context.assets.list("")
                    .filter { it.endsWith(".pls") }
                    .forEach { copyFile(it, appDir.path) }
            preferences.firstRun = false
        }
    }

    private fun initAppDir(): File {
        val appDir = Environment.getExternalStoragePublicDirectory("Radius")
        if (!appDir.mkdirs() && (!appDir.exists() || !appDir.isDirectory)) {
            throw IllegalStateException("Can not create playlist folder")
        }
        return appDir
    }

    private fun copyFile(filePath: String, destination: String) {
        Timber.e("copyFile: $filePath")
        context.assets.open(filePath).use { inS ->
            FileOutputStream(File(destination, filePath)).use { outS ->
                inS.copyTo(outS)
            }
        }
    }

    private fun fromNet(url: URL, cb: (Station?) -> Unit) {
        launch(UI) {
            val media = run(CommonPool) {
                val media = url.openStream().parsePls()
                media?.let { save(it) }
                media
            }
            cb(media)
        }
    }

    private fun fromFile(file: File): Station? {
        return when (file.extension) {
            "pls" -> file.parsePls()
            else -> null
        }
    }

    private fun fromFile(file: File, cb: (Station?) -> Unit) {
        val media = fromFile(file)
        media?.let { save(it) }
        cb(media)
    }


    private fun InputStream.parsePls(name: String = "default_name",
                                     dir: String = ""): Station? {
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
        if (uri == null) return null

        val parentPath = appDir.path + if (!dir.isEmpty()) "/$dir" else dir
        val path = "$parentPath/$title.pls"

        return Station(uri!!, path, fav)
    }

    private fun File.parsePls(): Station? = inputStream().parsePls(name,
            if (parentFile.name == "Radius") "" else parentFile.name)
}
