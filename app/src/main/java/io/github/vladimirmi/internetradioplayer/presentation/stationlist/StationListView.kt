package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

interface StationListView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun buildToolbar(builder: ToolbarBuilder)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setStations(stationList: FlatStationsList)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun selectStation(station: Station)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setPlaying(playing: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showToast(resId: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openAddStationDialog()
}
