package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 23.03.2019.
 */

class Data(val id: Int,
           val title: String,
           val subtitle: String,
           val uri: String) {

    var isFavorite = false
}