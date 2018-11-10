package io.github.vladimirmi.internetradioplayer.presentation.station

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface StationView : BaseView {

    fun setStation(station: Station)

    fun setEditMode(editMode: Boolean)

    fun editStation()

    fun createStation()

    fun openRemoveDialog()

    fun openLinkDialog(url: String)

    fun openCancelEditDialog()

    fun openCancelCreateDialog()

    fun openAddShortcutDialog()

    fun cancelEdit()
}
