package io.github.vladimirmi.internetradioplayer.model.service

import android.support.v4.media.session.MediaSessionCompat

/**
 * Created by Vladimir Mikhalev 12.12.2017.
 */

class SessionCallback(private val callback: Interface)
    : MediaSessionCompat.Callback() {

    override fun onPlay() {
        callback.onPlayCommand()
    }

    override fun onPause() {
        callback.onPauseCommand(stopDelay = STOP_DELAY) // 1 min
    }

    override fun onStop() {
        callback.onStopCommand()
    }

    override fun onSkipToPrevious() {
        callback.onSkipToPreviousCommand()
    }

    override fun onSkipToNext() {
        callback.onSkipToNextCommand()
    }

    interface Interface {
        fun onPlayCommand()

        fun onPauseCommand(stopDelay: Long)

        fun onStopCommand()

        fun onSkipToPreviousCommand()

        fun onSkipToNextCommand()
    }
}
