package io.github.vladimirmi.radius.presentation.playercontrol

import com.arellomobile.mvp.MvpView

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

interface PlayerControlView : MvpView {
    fun showBuffering()
    fun showStopped()
    fun showPlaying()
    fun setMediaInfo(info: String)
}