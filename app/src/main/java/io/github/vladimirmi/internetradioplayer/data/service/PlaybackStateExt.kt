package io.github.vladimirmi.internetradioplayer.data.service

import android.support.v4.media.session.PlaybackStateCompat

/**
 * Created by Vladimir Mikhalev 02.03.2019.
 */

val DEFAULT_PLAYBACK_STATE: PlaybackStateCompat
    get() = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1F)
            .setActions(PlayerActions.DEFAULT_ACTIONS).build()

fun PlaybackStateCompat.setState(newState: Int): PlaybackStateCompat {
    return PlaybackStateCompat.Builder(this)
            .setState(newState, position, playbackSpeed)
            .build()
}

fun PlaybackStateCompat.setPosition(newPositionMs: Long): PlaybackStateCompat {
    return PlaybackStateCompat.Builder(this)
            .setState(state, newPositionMs, playbackSpeed)
            .build()
}

fun PlaybackStateCompat.changeActions(changer: (Long) -> Long): PlaybackStateCompat {
    return PlaybackStateCompat.Builder(this)
            .setActions(changer(actions))
            .build()
}