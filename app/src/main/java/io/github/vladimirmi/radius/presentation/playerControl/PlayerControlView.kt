package io.github.vladimirmi.radius.presentation.playerControl

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import io.github.vladimirmi.radius.model.entity.Station

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

interface PlayerControlView : MvpView {

    fun showStopped()

    fun showPlaying()

    fun setStation(station: Station)

    fun enableNextPrevious(enable: Boolean)

    fun setStationIcon(stationIcon: Bitmap)
}