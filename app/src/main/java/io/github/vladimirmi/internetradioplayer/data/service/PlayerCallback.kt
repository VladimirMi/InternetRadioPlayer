package io.github.vladimirmi.internetradioplayer.data.service

import android.annotation.SuppressLint
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import timber.log.Timber
import java.net.ConnectException

const val EVENT_SESSION_START = "EVENT_SESSION_START"
const val EVENT_SESSION_END = "EVENT_SESSION_END"

abstract class PlayerCallback : Player.EventListener {

    var sessionId = 0

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        if (trackGroups.length > 0) {
            Timber.d("onTracksChanged: ${Format.toLogString(trackGroups.get(0).getFormat(0))}")
        }
    }

    @SuppressLint("SwitchIntDef")
    override fun onPlayerError(error: ExoPlaybackException) {
        val exception = when (error.type) {
            ExoPlaybackException.TYPE_RENDERER -> {
                RuntimeException("Renderer error occurred: ${error.rendererException.message}")
            }
            ExoPlaybackException.TYPE_SOURCE -> {
                ConnectException("Source error occurred: ${error.sourceException.message}")
            }
            else -> {
                RuntimeException("Unexpected error occurred: ${error.unexpectedException.message}")
            }
        }
        onPlayerError(exception)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val state = when {
            playbackState == Player.STATE_IDLE -> PlaybackStateCompat.STATE_STOPPED
            playbackState == Player.STATE_BUFFERING && playWhenReady -> PlaybackStateCompat.STATE_BUFFERING
            playbackState == Player.STATE_BUFFERING && !playWhenReady -> PlaybackStateCompat.STATE_PAUSED
            playbackState == Player.STATE_READY && playWhenReady -> PlaybackStateCompat.STATE_PLAYING
            playbackState == Player.STATE_READY && !playWhenReady -> PlaybackStateCompat.STATE_PAUSED
            playbackState == Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            else -> PlaybackStateCompat.STATE_NONE
        }
        if (state == PlaybackStateCompat.STATE_STOPPED) {
            onAudioSessionId(EVENT_SESSION_END, sessionId)
            sessionId = 0
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

    abstract fun onPlayerError(error: Exception)

    abstract fun onAudioSessionId(event: String, audioSessionId: Int)
}
