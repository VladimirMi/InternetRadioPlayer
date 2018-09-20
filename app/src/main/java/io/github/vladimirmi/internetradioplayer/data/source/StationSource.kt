package io.github.vladimirmi.internetradioplayer.data.source

import android.content.Context
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import java.io.File
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationSource
@Inject constructor(context: Context) {

    companion object {
        const val extension = "json"
    }

    private val appDir = context.getExternalFilesDir(null)

    fun removeStation(station: Station) {
        File(appDir, "${station.name}.$extension").delete()
    }
}
