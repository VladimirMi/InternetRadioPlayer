package io.github.vladimirmi.internetradioplayer.model.source

import android.content.Context
import android.net.Uri
import io.github.vladimirmi.internetradioplayer.extensions.clear
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.model.manager.StationParser
import java.io.File
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationSource
@Inject constructor(context: Context,
                    private val parser: StationParser) {

    companion object {
        const val extension = "json"
    }

    private val appDir = context.getExternalFilesDir(null)

    fun getStationList(): ArrayList<Station> {
        val stationList = ArrayList<Station>()
        val treeWalk = appDir.walkTopDown()
        treeWalk.forEach { file ->
            if (!file.isDirectory && file.extension == extension) {
                parser.parseFromJsonFile(file)?.let {
                    stationList.add(it)
                }
            }
        }
        return stationList
    }

    fun saveStation(station: Station) {
        val file = File(appDir, "${station.name}.$extension")
        if (file.exists()) file.clear()

        file.writeText(parser.toJson(station))
    }

    fun removeStation(station: Station) {
        File(appDir, "${station.name}.$extension").delete()
    }

    fun parseStation(uri: Uri): Station? = parser.parseFromUri(uri)
}
