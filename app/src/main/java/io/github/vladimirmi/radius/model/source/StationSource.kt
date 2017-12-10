package io.github.vladimirmi.radius.model.source

import android.net.Uri
import android.os.Environment
import io.github.vladimirmi.radius.extensions.clear
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.Preferences
import timber.log.Timber
import java.io.File
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationSource
@Inject constructor(private val preferences: Preferences) {

    private val appDir: File = run {
        val dir = Environment.getExternalStoragePublicDirectory("Radius")
        Timber.e(dir.path)
        if (!dir.mkdirs() && (!dir.exists() || !dir.isDirectory)) {
            throw IllegalStateException("Can not create playlist folder")
        }
        preferences.appDirPath = dir.path
        dir
    }

    fun getStationList(): ArrayList<Station> {
        val stationList = ArrayList<Station>()
        val treeWalk = appDir.walkTopDown()
        treeWalk.forEach { file ->
            if (!file.isDirectory) {
                Station.fromFile(file)?.let {
                    stationList.add(it)
                }
            }
        }
        return stationList
    }

    fun save(station: Station) {
        Timber.e("save: ${station.path}")
        with(File(station.path)) {
            if (exists() || parentFile.mkdirs() || createNewFile()) {
                clear()
                writeText(station.toContent())
            }
        }
    }

    fun remove(station: Station) {
        Timber.e("remove: ${station.path}")
        val file = File(station.path)
        file.delete()
        file.parentFile.delete()
    }

    fun parseStation(uri: Uri): Station? = Station.fromUri(uri)
}
