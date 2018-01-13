package io.github.vladimirmi.radius.presentation.playercontrol

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.radius.model.entity.Station

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

interface PlayerControlView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showStopped()

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showPlaying()

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setStation(station: Station)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun enableNextPrevious(enable: Boolean)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setStationIcon(stationIcon: Bitmap)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showToast(resId: Int)
}