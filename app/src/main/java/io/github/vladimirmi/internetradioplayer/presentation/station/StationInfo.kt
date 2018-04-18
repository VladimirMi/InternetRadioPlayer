package io.github.vladimirmi.internetradioplayer.presentation.station

import io.github.vladimirmi.internetradioplayer.model.entity.Station

/**
 * Created by Vladimir Mikhalev 20.02.2018.
 */

class StationInfo(
        val name: String,
        val group: String,
        val genre: List<String>
) {

    companion object {
        fun fromStation(station: Station): StationInfo {
            return StationInfo(station.name, station.group, station.genre)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StationInfo

        if (name != other.name) return false
        if (group != other.group) return false
        if (genre != other.genre) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + group.hashCode()
        result = 31 * result + genre.hashCode()
        return result
    }


}
