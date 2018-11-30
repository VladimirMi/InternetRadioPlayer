package io.github.vladimirmi.internetradioplayer.data.net.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */

class SearchResult(val success: Boolean, val result: List<StationSearchRes>)

class StationSearchRes(
        @SerializedName("station_id") val id: Int,
        val callsign: String,
        val genre: String,
        val band: String,
        val artist: String,
        val title: String
) {

    val uri = URI_BASE + id
}
