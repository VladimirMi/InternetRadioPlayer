package io.github.vladimirmi.internetradioplayer.data.service.player

import android.content.SharedPreferences
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.util.Util
import io.github.vladimirmi.internetradioplayer.data.utils.Preferences
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 22.09.2018.
 */

class LoadControl
@Inject constructor(private val prefs: Preferences)
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
    private var initialBufferUs = C.msToUs(prefs.initialBufferLength * 1000L)
    private var bufferUs = C.msToUs(prefs.bufferLength * 1000L)
    private val minBufferUs = C.msToUs(DEFAULT_MIN_BUFFER_MS.toLong())
    private val maxBufferUs = C.msToUs(DEFAULT_MAX_BUFFER_MS.toLong())
    private var isBuffering: Boolean = false


    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Preferences.KEY_INITIAL_BUFFER_LENGTH) {
            initialBufferUs = C.msToUs(prefs.initialBufferLength * 1000L)
            reset(true)

        } else if (key == Preferences.KEY_BUFFER_LENGTH) {
            bufferUs = C.msToUs(prefs.bufferLength * 1000L)
            reset(true)
        }
    }

    init {
        prefs.sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onTracksSelected(renderers: Array<Renderer>, trackGroups: TrackGroupArray,
                                  trackSelections: TrackSelectionArray) {
        targetBufferSize = calculateTargetBufferSize(renderers, trackSelections)
        (allocator as DefaultAllocator).setTargetBufferSize(targetBufferSize)
    }

    override fun shouldContinueLoading(bufferedDurationUs: Long, playbackSpeed: Float): Boolean {
        val targetBufferSizeReached = allocator.totalBytesAllocated >= targetBufferSize

        isBuffering = bufferedDurationUs < minBufferUs // below low watermark
                || (bufferedDurationUs <= maxBufferUs // between watermarks
                && isBuffering
                && !targetBufferSizeReached)

        return isBuffering
    }

    override fun shouldStartPlayback(bufferedDurationUs: Long,
                                     playbackSpeed: Float,
                                     rebuffering: Boolean): Boolean {

        val bufferDuration = Util.getPlayoutDurationForMediaDuration(bufferedDurationUs, playbackSpeed)
        val minBufferDuration = if (rebuffering) bufferUs else initialBufferUs
        return minBufferDuration <= 0 || bufferDuration >= minBufferDuration
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
        isBuffering = false
        if (resetAllocator) {
            (allocator as DefaultAllocator).reset()
        }
    }
}
