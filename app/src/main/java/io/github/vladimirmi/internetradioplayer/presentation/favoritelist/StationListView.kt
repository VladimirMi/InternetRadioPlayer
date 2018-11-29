package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

interface StationListView : BaseView {

    fun setStations(stationList: FlatStationsList)

    fun selectStation(station: Station)
}
