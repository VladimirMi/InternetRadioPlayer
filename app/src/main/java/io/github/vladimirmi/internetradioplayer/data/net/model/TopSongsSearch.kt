package io.github.vladimirmi.internetradioplayer.data.net.model

import com.google.gson.annotations.SerializedName


/**
 * Created by Vladimir Mikhalev 23.03.2019.
 */

class TopSongsSearch(
        val result: List<TopSongsResult>,
        val success: Boolean
)

class TopSongsResult(
        @SerializedName("callsign") val callsign: String,
        @SerializedName("songartist") val artist: String,
        @SerializedName("songtitle") val title: String,
        @SerializedName("station_id") val id: String
) {

    val uri get() = "$URI_BASE$id"
}