package io.github.vladimirmi.internetradioplayer.data.net.model

import com.google.gson.annotations.SerializedName
import io.github.vladimirmi.internetradioplayer.domain.model.Data


/**
 * Created by Vladimir Mikhalev 24.03.2019.
 */

class TalksSearch(
        @SerializedName("stations")
        val result: List<TalkResult>
)

class TalkResult(
        @SerializedName("showgenre") val genre: String,
        @SerializedName("showid") val id: String,
        @SerializedName("showname") val title: String
) {

    companion object {
        private const val PODCAST_PREFIX = "RSS_"
    }

    fun toData(): Data {
        val isPodcast = id.startsWith(PODCAST_PREFIX)
        val stationId = if (isPodcast) id.substringAfter(PODCAST_PREFIX).toInt()
        else id.toInt()
        val subtitle = if (isPodcast) "Podcast" else "Live"

        return Data(
                stationId = stationId,
                id = id,
                title = title,
                subtitle = subtitle,
                uri = ""
        )
    }
}