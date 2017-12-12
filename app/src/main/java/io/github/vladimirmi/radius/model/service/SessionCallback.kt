package io.github.vladimirmi.radius.model.service

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat

/**
 * Created by Vladimir Mikhalev 12.12.2017.
 */

class SessionCallback(private val callback: Interface)
    : MediaSessionCompat.Callback() {

    override fun onPlay() {
        callback.onPlay()
    }

    override fun onPause() {
        callback.onPause()
    }

    override fun onStop() {
        callback.onStop()
    }

    override fun onSkipToPrevious() {
        callback.onSkipToPrevious()
    }

    override fun onSkipToNext() {
        callback.onSkipToNext()
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        callback.onCustomAction(action, extras)
    }

    interface Interface {
        fun onPlay()

        fun onPause()

        fun onStop()

        fun onSkipToPrevious()

        fun onSkipToNext()

        fun onCustomAction(action: String?, extras: Bundle?)
    }
}