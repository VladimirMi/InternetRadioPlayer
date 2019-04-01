package io.github.vladimirmi.internetradioplayer.domain.model

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station

/**
 * Created by Vladimir Mikhalev 01.04.2019.
 */

class MediaInfo(val group: String?,
                val specs: String?,
                val slogan: String? = null,
                val description: String? = null,
                val genre: String? = null,
                val language: String? = null,
                val location: String? = null,
                val website: String? = null) {

    companion object {

        fun nullObj(): MediaInfo {
            return MediaInfo(null, null)
        }

        fun fromStation(station: Station): MediaInfo {
            return MediaInfo(group = "", specs = station.specs)
        }
    }

    fun update(info: MediaInfo): MediaInfo {
        return MediaInfo(
                group = info.group ?: group,
                specs = info.specs ?: specs,
                slogan = info.slogan ?: slogan,
                description = info.description ?: description,
                genre = info.genre ?: genre,
                language = info.language ?: language,
                location = info.location ?: location,
                website = info.website ?: website
        )
    }
}
