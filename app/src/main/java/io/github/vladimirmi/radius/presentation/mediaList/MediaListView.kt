package io.github.vladimirmi.radius.presentation.mediaList

import com.arellomobile.mvp.MvpView
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

interface MediaListView : MvpView {

    fun setMediaList(stationList: GroupedList<Station>)

    fun selectItem(station: Station, playing: Boolean)

    fun showToast(resId: Int)

    fun buildToolbar(builder: ToolbarBuilder)
}