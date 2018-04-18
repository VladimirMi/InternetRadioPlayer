package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.model.entity.groupedlist.GroupedList
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

interface StationListView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun buildToolbar(builder: ToolbarBuilder)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setMediaList(stationList: GroupedList<Station>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun selectItem(station: Station, playing: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showToast(resId: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openAddStationDialog()
}
