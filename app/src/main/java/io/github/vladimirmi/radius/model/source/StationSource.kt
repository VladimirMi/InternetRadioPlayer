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
        treeWalk.forEach { file ->
            if (!file.isDirectory) {
                Timber.e("file ${file.path}")
                Station.fromFile(file)?.let { stationList.add(it) }
            }
        }
        return stationList
    }

    fun save(station: Station) {
        with(File(station.path)) {
            if (exists() || parentFile.mkdirs()) {
                clear()
                writeText(station.toContent())
            }
        }
    }

    fun remove(station: Station) {
        val file = File(station.path)
        file.delete()
        file.parentFile.delete()
    }

    fun parseStation(uri: Uri): Station? = Station.fromUri(uri)

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
