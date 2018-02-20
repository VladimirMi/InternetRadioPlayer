package io.github.vladimirmi.internetradioplayer.presentation.station

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface StationView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setStation(station: Station)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setStationIcon(icon: Bitmap)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun buildToolbar(builder: ToolbarBuilder)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setEditMode(editMode: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun editStation()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun createStation()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openRemoveDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openLinkDialog(url: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openCancelEditDialog(currentStation: StationInfo, iconChanged: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openCancelCreateDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showToast(resId: Int)
}
