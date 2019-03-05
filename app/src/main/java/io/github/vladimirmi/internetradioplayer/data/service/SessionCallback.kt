package io.github.vladimirmi.internetradioplayer.data.service

import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat

/**
 * Created by Vladimir Mikhalev 12.12.2017.
 */

const val COMMAND_ENABLE_SKIP = "COMMAND_ENABLE_SKIP"
const val COMMAND_DISABLE_SKIP = "COMMAND_DISABLE_SKIP"
const val COMMAND_ENABLE_SEEK = "COMMAND_ENABLE_SEEK"
const val COMMAND_DISABLE_SEEK = "COMMAND_DISABLE_SEEK"

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

    override fun onSeekTo(pos: Long) {
        callback.onSeekCommand(pos)
    }

    override fun onCommand(command: String, extras: Bundle?, cb: ResultReceiver?) {
        val changer: (Long) -> Long = when (command) {
            COMMAND_ENABLE_SKIP -> PlayerActions::enableSkip
            COMMAND_DISABLE_SKIP -> PlayerActions::disableSkip
            COMMAND_ENABLE_SEEK -> PlayerActions::enableSeek
            COMMAND_DISABLE_SEEK -> PlayerActions::disableSeek
            else -> { it -> it }
        }
        callback.onActionsChangeCommand(changer)
    }

    interface Interface {
        fun onPlayCommand()

        fun onPauseCommand(stopDelay: Long)

        fun onStopCommand()

        fun onSkipToPreviousCommand()

        fun onSkipToNextCommand()

        fun onSeekCommand(pos: Long)

        fun onActionsChangeCommand(changer: (Long) -> Long)
    }
}
