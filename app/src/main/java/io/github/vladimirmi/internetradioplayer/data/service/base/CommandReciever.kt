package io.github.vladimirmi.internetradioplayer.data.service.base

import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.Nullable
import com.google.android.exoplayer2.Player

/**
 * Created by Vladimir Mikhalev 04.02.2019.
 */

/** Receiver of media commands sent by a media controller.  */
interface CommandReceiver {

    /**
     * Returns the commands the receiver handles, or `null` if no commands need to be handled.
     */
    val commands: Array<String>?

    /** See [MediaSessionCompat.Callback.onCommand].  */
    fun onCommand(player: Player, command: String, extras: Bundle, cb: ResultReceiver)
}

/** Interface to which playback actions are delegated.  */
interface PlaybackController : CommandReceiver {

    /**
     * Returns the actions which are supported by the controller. The supported actions must be a
     * bitmask combined out of [PlaybackStateCompat.ACTION_PLAY_PAUSE],
     * [PlaybackStateCompat.ACTION_PLAY], [PlaybackStateCompat.ACTION_PAUSE],
     * [PlaybackStateCompat.ACTION_SEEK_TO], [PlaybackStateCompat.ACTION_FAST_FORWARD],
     * [PlaybackStateCompat.ACTION_REWIND], [PlaybackStateCompat.ACTION_STOP],
     * [PlaybackStateCompat.ACTION_SET_REPEAT_MODE] and
     * [PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE].
     *
     * @param player The player.
     * @return The bitmask of the supported media actions.
     */
    fun getSupportedPlaybackActions(@Nullable player: Player): Long

    /** See [MediaSessionCompat.Callback.onPlay].  */
    fun onPlay(player: Player)

    /** See [MediaSessionCompat.Callback.onPause].  */
    fun onPause(player: Player)

    /** See [MediaSessionCompat.Callback.onSeekTo].  */
    fun onSeekTo(player: Player, position: Long)

    /** See [MediaSessionCompat.Callback.onFastForward].  */
    fun onFastForward(player: Player)

    /** See [MediaSessionCompat.Callback.onRewind].  */
    fun onRewind(player: Player)

    /** See [MediaSessionCompat.Callback.onStop].  */
    fun onStop(player: Player)

    /** See [MediaSessionCompat.Callback.onSetShuffleMode].  */
    fun onSetShuffleMode(player: Player, shuffleMode: Int)

    /** See [MediaSessionCompat.Callback.onSetRepeatMode].  */
    fun onSetRepeatMode(player: Player, repeatMode: Int)

    companion object {

        val ACTIONS = (
                PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SEEK_TO
                        or PlaybackStateCompat.ACTION_FAST_FORWARD
                        or PlaybackStateCompat.ACTION_REWIND
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                        or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
    }
}

/**
 * Handles queue navigation actions, and updates the media session queue by calling `MediaSessionCompat.setQueue()`.
 */
interface QueueNavigator : CommandReceiver {

    /**
     * Returns the actions which are supported by the navigator. The supported actions must be a
     * bitmask combined out of [PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM],
     * [PlaybackStateCompat.ACTION_SKIP_TO_NEXT],
     * [PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS].
     *
     * @param player The [Player].
     * @return The bitmask of the supported media actions.
     */
    fun getSupportedQueueNavigatorActions(@Nullable player: Player): Long

    /**
     * Called when the timeline of the player has changed.
     *
     * @param player The player of which the timeline has changed.
     */
    fun onTimelineChanged(player: Player)

    /**
     * Called when the current window index changed.
     *
     * @param player The player of which the current window index of the timeline has changed.
     */
    fun onCurrentWindowIndexChanged(player: Player)

    /**
     * Gets the id of the currently active queue item, or
     * [MediaSessionCompat.QueueItem.UNKNOWN_ID] if the active item is unknown.
     *
     *
     * To let the connector publish metadata for the active queue item, the queue item with the
     * returned id must be available in the list of items returned by
     * [MediaControllerCompat.getQueue].
     *
     * @param player The player connected to the media session.
     * @return The id of the active queue item.
     */
    fun getActiveQueueItemId(@Nullable player: Player): Long

    /** See [MediaSessionCompat.Callback.onSkipToPrevious].  */
    fun onSkipToPrevious(player: Player)

    /** See [MediaSessionCompat.Callback.onSkipToQueueItem].  */
    fun onSkipToQueueItem(player: Player, id: Long)

    /** See [MediaSessionCompat.Callback.onSkipToNext].  */
    fun onSkipToNext(player: Player)

    companion object {

        val ACTIONS = (
                PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    }
}

/** Handles media session queue edits.  */
interface QueueEditor : CommandReceiver {

    /**
     * See [MediaSessionCompat.Callback.onAddQueueItem].
     */
    fun onAddQueueItem(player: Player, description: MediaDescriptionCompat)

    /**
     * See [MediaSessionCompat.Callback.onAddQueueItem].
     */
    fun onAddQueueItem(player: Player, description: MediaDescriptionCompat, index: Int)

    /**
     * See [MediaSessionCompat.Callback.onRemoveQueueItem].
     */
    fun onRemoveQueueItem(player: Player, description: MediaDescriptionCompat)
}