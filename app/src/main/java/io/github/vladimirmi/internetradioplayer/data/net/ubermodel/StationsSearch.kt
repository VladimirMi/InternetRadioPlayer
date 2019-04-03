package io.github.vladimirmi.internetradioplayer.data.net.ubermodel

import com.google.gson.annotations.SerializedName
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import java.util.*

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

class StationsSearch(val result: List<StationResult>)

class StationResult(
        @SerializedName("station_id") val id: Int,
        val callsign: String,
        val genre: String,
        val artist: String,
        val title: String
) {

    val uri get() = "$URI_BASE$id"

    fun toStation(): Station {
        return Station(
                id = UUID.randomUUID().toString(),
                remoteId = id.toString(),
                name = callsign,
                uri = uri,
                description = "$artist - $title"
        )
    }
}
