package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 23.03.2019.
 */

//todo rename to MediaData
data class Data(val stationId: Int,
                val id: String = stationId.toString(),
                val title: String,
                val subtitle: String,
                val uri: String) {

    var isFavorite = false
}