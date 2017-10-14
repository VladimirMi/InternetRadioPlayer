package io.github.vladimirmi.radius.service

/**
 * Created by Vladimir Mikhalev 19.07.2017.
 */

interface PlaybackCallback {
    /**
     * On current music completed.
     */
    fun onCompletion()

    /**
     * on Playback status changed
     * Implementations can use this callback to update
     * playback state on the media sessions.
     */
    fun onPlaybackStatusChanged(state: Int)

    /**
     * @param error to be added to the PlaybackState
     */
    fun onError(error: String)

}