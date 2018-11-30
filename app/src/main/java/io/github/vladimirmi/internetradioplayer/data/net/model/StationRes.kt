package io.github.vladimirmi.internetradioplayer.data.net.model

import com.google.gson.annotations.SerializedName
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station

/**
 * Created by Vladimir Mikhalev 15.11.2018.
 */
const val URI_BASE = "http://stream.dar.fm/"

class StationRes(
        @SerializedName("station_id") val id: Int,
        @SerializedName("callsign") val name: String,
        @SerializedName("band") val band: String,
        @SerializedName("ubergenre") val genre: String,
        @SerializedName("language") val language: String,
        @SerializedName("websiteurl") val websiteUrl: String,
        @SerializedName("imageurl") val imageUrl: String,
        @SerializedName("description") val description: String,
        @SerializedName("encoding") val encoding: String,
        @SerializedName("status") val status: String,
        @SerializedName("country") val countryCode: String,
        @SerializedName("city") val city: String,
        @SerializedName("phone") val phone: String,
        @SerializedName("email") val email: String,
        @SerializedName("dial") val dial: String,
        @SerializedName("slogan") val slogan: String
) {

    fun toStation(): Station {
        val uri = URI_BASE + id
        return Station(name = name, uri = uri, genre = genre,
                url = websiteUrl, encoding = encoding,
                bitrate = null, sample = null)
    }
}
