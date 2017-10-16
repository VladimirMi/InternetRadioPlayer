package io.github.vladimirmi.radius.service

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray


open class EmptyPlayerCallback : Player.EventListener {

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

    override fun onPlayerError(error: ExoPlaybackException?) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

    override fun onLoadingChanged(isLoading: Boolean) {}

    override fun onPositionDiscontinuity() {}

    override fun onRepeatModeChanged(repeatMode: Int) {}

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {}
}