package io.github.vladimirmi.internetradioplayer.data.net.ubermodel

import com.google.gson.annotations.SerializedName
import io.github.vladimirmi.internetradioplayer.domain.model.Talk


/**
 * Created by Vladimir Mikhalev 24.03.2019.
 */

class TalkIdSearch(
        @SerializedName("result") val result: List<TalkIdResult>
)

class TalkIdResult(
        @SerializedName("url") val uri: String,
        @SerializedName("encoding") val encoding: String,
        @SerializedName("callsign") val callsign: String,
        @SerializedName("websiteurl") val websiteUrl: String,
        @SerializedName("timeleft") val timeleft: Int,
        @SerializedName("timeplayed") val timeplayed: Int
) {

    fun toTalk(source: Talk): Talk {
        return source.copy(
                name = callsign,
                uri = uri,
                timeleft = timeleft,
                timeplayed = timeplayed,
                website = websiteUrl
        )
    }
}