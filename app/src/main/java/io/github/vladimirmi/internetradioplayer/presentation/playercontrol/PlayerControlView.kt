package io.github.vladimirmi.internetradioplayer.presentation.playercontrol

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

interface PlayerControlView : BaseView {

    fun showStopped()

    fun showPlaying()

    fun showLoading()

    fun setStation(station: Station)

    fun enableEditMode(enable: Boolean)

    fun setMetadata(metadata: String)

    fun showNext()

    fun showPrevious()
}
