package io.github.vladimirmi.internetradioplayer.presentation.history

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

interface HistoryView : BaseView {

    fun setHistory(list: List<Pair<Station, Boolean>>)

    fun selectStation(id: String)

    fun showPlaceholder(show: Boolean)
}