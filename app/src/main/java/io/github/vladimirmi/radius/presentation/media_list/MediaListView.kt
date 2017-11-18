package io.github.vladimirmi.radius.presentation.media_list

import com.arellomobile.mvp.MvpView
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Station

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

interface MediaListView : MvpView {
    fun setMediaList(stationList: GroupedList<Station>)
    fun select(station: Station, playing: Boolean)
    fun notifyList()
    fun showToast(resId: Int)
    fun openAddDialog(station: Station)
    fun closeAddDialog()
    fun openRemoveDialog(station: Station)
}