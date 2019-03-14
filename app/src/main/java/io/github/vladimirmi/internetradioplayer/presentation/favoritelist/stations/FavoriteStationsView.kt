package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

interface FavoriteStationsView : BaseView {

    fun setStations(stationList: FlatStationsList)

    fun selectStation(uri: String)

    fun showPlaceholder(show: Boolean)
}