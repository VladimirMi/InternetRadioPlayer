package io.github.vladimirmi.internetradioplayer.model.service

import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import timber.log.Timber


open class PlayerCallback : Player.EventListener {

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        if (trackGroups.length > 0) {
            Timber.d("onTracksChanged: ${Format.toLogString(trackGroups.get(0).getFormat(0))}")
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        when (error.type) {
            ExoPlaybackException.TYPE_RENDERER -> Timber.e("RENDERER error occurred: ${error.rendererException}")
            ExoPlaybackException.TYPE_SOURCE -> Timber.e("SOURCE error occurred: ${error.sourceException}")
            ExoPlaybackException.TYPE_UNEXPECTED -> Timber.e("UNEXPECTED error occurred: ${error.unexpectedException}")
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Timber.d("onPlayerStateChanged: $playWhenReady $playbackState")
    }

    override fun onLoadingChanged(isLoading: Boolean) {}

    override fun onPositionDiscontinuity() {}

    override fun onRepeatModeChanged(repeatMode: Int) {}

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?) {}

    open fun onMetadata(metadata: Metadata) {
        Timber.d("onMetadata ${metadata.toLogString()}")
    }
}