package io.github.vladimirmi.internetradioplayer.data.service

import android.annotation.SuppressLint
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import timber.log.Timber


abstract class PlayerCallback : Player.EventListener {

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        if (trackGroups.length > 0) {
            Timber.d("onTracksChanged: ${Format.toLogString(trackGroups.get(0).getFormat(0))}")
        }
    }

    @SuppressLint("SwitchIntDef")
    override fun onPlayerError(error: ExoPlaybackException) {
        val messageId = when (error.type) {
            ExoPlaybackException.TYPE_RENDERER -> {
                Timber.w("RENDERER error occurred: ${error.rendererException}")
                0
            }
            ExoPlaybackException.TYPE_SOURCE -> {
                Timber.w("SOURCE error occurred: ${error.sourceException}")
                1
            }
            else -> {
                Timber.w("UNEXPECTED error occurred: ${error.unexpectedException}")
                2
            }
        }
        onPlayerError(messageId)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val state = when (playbackState) {
            Player.STATE_IDLE -> PlaybackStateCompat.STATE_STOPPED
            Player.STATE_BUFFERING -> if (playWhenReady) PlaybackStateCompat.STATE_BUFFERING else PlaybackStateCompat.STATE_PAUSED
            Player.STATE_READY -> if (playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            else -> PlaybackStateCompat.STATE_NONE
        }
        onPlayerStateChanged(state)
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onSeekProcessed() {
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
    }

    abstract fun onPlayerStateChanged(state: Int)

    abstract fun onMetadata(metadata: String)

    abstract fun onPlayerError(error: Int)
}
