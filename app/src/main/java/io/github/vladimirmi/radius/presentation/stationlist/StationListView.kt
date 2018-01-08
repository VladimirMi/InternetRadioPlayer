package io.github.vladimirmi.radius.presentation.stationlist

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.radius.model.entity.GroupedList.GroupedList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

interface StationListView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setMediaList(stationList: GroupedList<Station>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun selectItem(station: Station, playing: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showToast(resId: Int)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun buildToolbar(builder: ToolbarBuilder)
}