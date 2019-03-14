package io.github.vladimirmi.internetradioplayer.data.service.recorder

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultAllocator

/**
 * Created by Vladimir Mikhalev 11.03.2019.
 */

class RecorderLoadControl
    : DefaultLoadControl(DefaultAllocator(true, C.DEFAULT_AUDIO_BUFFER_SIZE),
        DEFAULT_MIN_BUFFER_MS,
        DEFAULT_MAX_BUFFER_MS,
        DEFAULT_BUFFER_FOR_PLAYBACK_MS,
        DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
        DEFAULT_TARGET_BUFFER_BYTES,
        DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS,
        null,
        DEFAULT_BACK_BUFFER_DURATION_MS,
        DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME) {

    private var targetBufferSize = DEFAULT_TARGET_BUFFER_BYTES


    override fun onTracksSelected(renderers: Array<Renderer>, trackGroups: TrackGroupArray,
                                  trackSelections: TrackSelectionArray) {
        targetBufferSize = calculateTargetBufferSize(renderers, trackSelections)
        (allocator as DefaultAllocator).setTargetBufferSize(targetBufferSize)
    }

    override fun shouldContinueLoading(bufferedDurationUs: Long, playbackSpeed: Float): Boolean {
        return true
    }

    override fun shouldStartPlayback(bufferedDurationUs: Long,
                                     playbackSpeed: Float,
                                     rebuffering: Boolean): Boolean {
        return false
    }

    override fun onPrepared() {
        reset(false)
    }

    override fun onStopped() {
        reset(true)
    }

    override fun onReleased() {
        reset(true)
    }

    private fun reset(resetAllocator: Boolean) {
        targetBufferSize = 0
        if (resetAllocator) {
            (allocator as DefaultAllocator).reset()
        }
    }
}
