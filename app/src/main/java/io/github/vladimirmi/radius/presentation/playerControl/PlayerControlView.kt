package io.github.vladimirmi.radius.presentation.playerControl

import com.arellomobile.mvp.MvpView
import io.github.vladimirmi.radius.model.entity.Station

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

interface PlayerControlView : MvpView {
    fun showBuffering()
    fun showStopped()
    fun showPlaying()
    fun setMediaInfo(info: String)
    fun setMedia(station: Station)
}