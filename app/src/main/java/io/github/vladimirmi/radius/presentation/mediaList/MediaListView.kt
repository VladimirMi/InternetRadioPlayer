package io.github.vladimirmi.radius.presentation.mediaList

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

interface MediaListView : MvpView {

    fun setMediaList(stationList: GroupedList<Station>)

    fun selectItem(station: Station, playing: Boolean)

    fun notifyList()

    fun showToast(resId: Int)

    fun openAddDialog(station: Station)

    fun closeAddDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openRemoveDialog(station: Station)

    fun buildToolbar(builder: ToolbarBuilder)

    fun closeRemoveDialog()
}