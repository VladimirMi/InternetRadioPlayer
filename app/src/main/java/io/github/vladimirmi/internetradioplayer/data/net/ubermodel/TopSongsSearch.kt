package io.github.vladimirmi.internetradioplayer.data.net.ubermodel

import com.google.gson.annotations.SerializedName
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import java.util.*


/**
 * Created by Vladimir Mikhalev 23.03.2019.
 */

class TopSongsSearch(val result: List<TopSongResult>)

class TopSongResult(
        @SerializedName("callsign") val callsign: String,
        @SerializedName("songartist") val artist: String,
        @SerializedName("songtitle") val title: String,
        @SerializedName("station_id") val id: Int
) {

    val uri get() = "$URI_BASE$id"

    fun toStation(): Station {
        return Station(
                id = UUID.randomUUID().toString(),
                remoteId = id.toString(),
                uri = uri,
                name = callsign,
                description = "$artist - $title"
        )
    }
}