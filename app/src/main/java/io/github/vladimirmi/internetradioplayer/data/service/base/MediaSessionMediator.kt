package io.github.vladimirmi.internetradioplayer.data.service.base

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline

/**
 * Created by Vladimir Mikhalev 04.02.2019.
 */

class MediaSessionMediator(
        private val playbackController: PlaybackController,
        private val queueNavigator: QueueNavigator,
        private val queueEditor: QueueEditor
) {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat

    private fun invalidateMediaSessionPlaybackState() {
        TODO("not implemented")
    }

    private fun invalidateMediaSessionMetadata() {
        TODO("not implemented")
    }

    private fun buildPlaybackActions(): Long {
        var actions = PlaybackController.ACTIONS and playbackController.getSupportedPlaybackActions(player)
        actions = actions or (QueueNavigator.ACTIONS and queueNavigator.getSupportedQueueNavigatorActions(player))
        return actions
    }

    private inner class ExoPlayerEventListener : Player.EventListener {

        private var currentWindowIndex: Int = 0

        override fun onTimelineChanged(
                timeline: Timeline, manifest: Any?, @Player.TimelineChangeReason reason: Int) {
            val windowIndex = player.currentWindowIndex
            queueNavigator.onTimelineChanged(player)
            invalidateMediaSessionPlaybackState()
            currentWindowIndex = windowIndex
            invalidateMediaSessionMetadata()
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            invalidateMediaSessionPlaybackState()
        }

        override fun onRepeatModeChanged(@Player.RepeatMode repeatMode: Int) {
            mediaSession.setRepeatMode(
                    if (repeatMode == Player.REPEAT_MODE_ONE)
                        PlaybackStateCompat.REPEAT_MODE_ONE
                    else if (repeatMode == Player.REPEAT_MODE_ALL)
                        PlaybackStateCompat.REPEAT_MODE_ALL
                    else
                        PlaybackStateCompat.REPEAT_MODE_NONE)
            invalidateMediaSessionPlaybackState()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            mediaSession.setShuffleMode(
                    if (shuffleModeEnabled)
                        PlaybackStateCompat.SHUFFLE_MODE_ALL
                    else
                        PlaybackStateCompat.SHUFFLE_MODE_NONE)
            invalidateMediaSessionPlaybackState()
        }

        override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
            if (currentWindowIndex != player.currentWindowIndex) {
                queueNavigator.onCurrentWindowIndexChanged(player)
                currentWindowIndex = player.currentWindowIndex
                // Update playback state after queueNavigator.onCurrentWindowIndexChanged has been called
                // and before updating metadata.
                invalidateMediaSessionPlaybackState()
                invalidateMediaSessionMetadata()
                return
            }
            invalidateMediaSessionPlaybackState()
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            invalidateMediaSessionPlaybackState()
        }
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            playbackController.onPlay(player)
        }

        override fun onPause() {
            playbackController.onPause(player)
        }

        override fun onSeekTo(position: Long) {
            playbackController.onSeekTo(player, position)
        }

        override fun onFastForward() {
            playbackController.onFastForward(player)
        }

        override fun onRewind() {
            playbackController.onRewind(player)
        }

        override fun onStop() {
            playbackController.onStop(player)
        }

        override fun onSkipToNext() {
            queueNavigator.onSkipToNext(player)
        }

        override fun onSkipToPrevious() {
            queueNavigator.onSkipToPrevious(player)
        }

        override fun onSkipToQueueItem(id: Long) {
            queueNavigator.onSkipToQueueItem(player, id)
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat) {
            queueEditor.onAddQueueItem(player, description)
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat, index: Int) {
            queueEditor.onAddQueueItem(player, description, index)
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat) {
            queueEditor.onRemoveQueueItem(player, description)
        }
    }
}