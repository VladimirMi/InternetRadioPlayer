package io.github.vladimirmi.radius.presentation.station

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface StationView : MvpView {

    fun setStation(station: Station)

    fun buildToolbar(builder: ToolbarBuilder)

    fun setEditMode(editMode: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openSaveDialog()

    fun closeSaveDialog()

    fun openDeleteDialog()

    fun closeDeleteDialog()

    fun openLinkDialog(url: String)

    fun closeLinkDialog()

    fun openCancelEditDialog()

    fun closeCancelEditDialog()

    fun openCreateDialog()

    fun closeCreateDialog()

    fun openCancelCreateDialog()

    fun closeCancelCreateDialog()

    fun showToast(resId: Int)
}