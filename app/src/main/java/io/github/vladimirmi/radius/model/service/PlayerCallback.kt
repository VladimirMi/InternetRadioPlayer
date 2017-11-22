package io.github.vladimirmi.radius.model.service

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import timber.log.Timber


abstract class PlayerCallback : Player.EventListener {

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        Timber.d("onPlaybackParametersChanged: ")
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        Timber.d("onTracksChanged: ")
    }

    override fun onPlayerError(error: ExoPlaybackException?) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

    override fun onLoadingChanged(isLoading: Boolean) {
        Timber.d("onLoadingChanged: $isLoading")
    }

    override fun onPositionDiscontinuity() {}

    override fun onRepeatModeChanged(repeatMode: Int) {}

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
        Timber.d("onTimelineChanged: $timeline")
    }

    abstract fun onMetadata(key: String, value: String)


}