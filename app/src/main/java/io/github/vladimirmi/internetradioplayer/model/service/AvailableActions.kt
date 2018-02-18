package io.github.vladimirmi.internetradioplayer.model.service

import android.support.v4.media.session.PlaybackStateCompat

/**
 * Created by Vladimir Mikhalev 20.12.2017.
 */

object AvailableActions {

    const val NEXT_PREVIOUS_ENABLED: Long = (PlaybackStateCompat.ACTION_PLAY_PAUSE
            or PlaybackStateCompat.ACTION_STOP
            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)

    const val NEXT_PREVIOUS_DISABLED: Long = (PlaybackStateCompat.ACTION_PLAY_PAUSE
            or PlaybackStateCompat.ACTION_STOP)

    fun isNextPreviousEnabled(actions: Long): Boolean {
        return actions == NEXT_PREVIOUS_ENABLED
    }
}
