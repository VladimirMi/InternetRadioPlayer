package io.github.vladimirmi.internetradioplayer.data.net.ubermodel

import com.google.gson.annotations.SerializedName
import io.github.vladimirmi.internetradioplayer.domain.model.Data

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

    fun toData(): Data {
        return Data(
                stationId = id,
                title = callsign,
                subtitle = "$artist - $title",
                uri = uri
        )
    }
}
