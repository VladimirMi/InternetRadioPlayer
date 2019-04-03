package io.github.vladimirmi.internetradioplayer.data.net.ubermodel

import com.google.gson.annotations.SerializedName
import io.github.vladimirmi.internetradioplayer.domain.model.Talk
import java.util.*


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

    fun toTalk(): Talk {
        val isPodcast = id.startsWith(PODCAST_PREFIX)
        val subtitle = if (isPodcast) "Podcast" else "Live"

        return Talk(
                id = UUID.randomUUID().toString(),
                remoteId = id,
                uri = "",
                name = title,
                description = subtitle
        )
    }
}