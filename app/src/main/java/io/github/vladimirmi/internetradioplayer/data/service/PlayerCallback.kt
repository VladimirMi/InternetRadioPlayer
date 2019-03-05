package io.github.vladimirmi.internetradioplayer.data.service

import android.annotation.SuppressLint
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import timber.log.Timber
import java.net.ConnectException

const val EVENT_SESSION_START = "EVENT_SESSION_START"
const val EVENT_SESSION_END = "EVENT_SESSION_END"

abstract class PlayerCallback : Player.EventListener {

    private var sessionId = 0
    private var playbackStateCompat = DEFAULT_PLAYBACK_STATE
    private var mediaMetadata = EMPTY_METADATA
    var player: ExoPlayer? = null

    fun initDefault() {
        onPlaybackStateChanged(playbackStateCompat)
        onMediaMetadataChanged(mediaMetadata)
    }

    fun setStartAudioSessionId(audioSessionId: Int) {
        sessionId = audioSessionId
        onAudioSessionId(EVENT_SESSION_START, audioSessionId)
    }

    fun setMedia(media: Media) {
        mediaMetadata = EMPTY_METADATA.setMedia(media)
        onMediaMetadataChanged(mediaMetadata)
    }

    fun setArtistTitle(artistTitle: String) {
        mediaMetadata = mediaMetadata.setArtistTitle(artistTitle)
        onMediaMetadataChanged(mediaMetadata)
    }

    fun changeActions(changer: (Long) -> Long) {
        playbackStateCompat = playbackStateCompat.changeActions(changer)
        onPlaybackStateChanged(playbackStateCompat)
    }

    //region =============== Player.EventListener ==============

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        Timber.e("onPlaybackParametersChanged: ${playbackParameters.speed}")
    }

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
            playbackState == Player.STATE_ENDED -> {
                player?.stop() //todo implement "play next"
                PlaybackStateCompat.STATE_STOPPED
            }
            else -> PlaybackStateCompat.STATE_NONE
        }
        playbackStateCompat = playbackStateCompat.setState(state)

        if (playbackState == Player.STATE_READY) {
            mediaMetadata = mediaMetadata.setDuration(player?.duration ?: 0)
            playbackStateCompat = playbackStateCompat.setPosition(player?.currentPosition ?: 0)
        }

        if (state == PlaybackStateCompat.STATE_STOPPED) {
            playbackStateCompat = playbackStateCompat.setPosition(0)
            onAudioSessionId(EVENT_SESSION_END, sessionId)
            sessionId = 0
            mediaMetadata = mediaMetadata.clearArtistTitle()
        }

        onPlaybackStateChanged(playbackStateCompat)
        onMediaMetadataChanged(mediaMetadata)
    }

    override fun onSeekProcessed() {
        player?.let {
            playbackStateCompat = playbackStateCompat.setPosition(it.currentPosition)
            onPlaybackStateChanged(playbackStateCompat)
        }
    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    //endregion

    protected abstract fun onPlayerError(error: Exception)

    protected abstract fun onAudioSessionId(event: String, audioSessionId: Int)

    protected abstract fun onPlaybackStateChanged(state: PlaybackStateCompat)

    protected abstract fun onMediaMetadataChanged(mediaMetadata: MediaMetadataCompat)
}
