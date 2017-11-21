package io.github.vladimirmi.radius.model.service

import android.net.Uri
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray


abstract class PlayerCallback : Player.EventListener {

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

    override fun onPlayerError(error: ExoPlaybackException?) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

    override fun onLoadingChanged(isLoading: Boolean) {}

    override fun onPositionDiscontinuity() {}

    override fun onRepeatModeChanged(repeatMode: Int) {}

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {}

    abstract fun onMetadata(key: String, value: String)

    abstract fun onPlayUri(uri: Uri)
}