package io.github.vladimirmi.radius.model.source

import android.content.Context
import android.net.Uri
import android.os.Environment
import io.github.vladimirmi.radius.extensions.clear
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.Preferences
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
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
                //todo can throw error
                stationList.add(Station.fromFile(it))
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

    fun parseStation(uri: Uri): Station {
        return Station.fromUri(uri)
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
        preferences.appDirPath = appDir.path
        Timber.e("initAppDir: ${appDir.path}")
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
}
