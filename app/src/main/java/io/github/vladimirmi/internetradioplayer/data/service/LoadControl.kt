package io.github.vladimirmi.internetradioplayer.data.service

import android.content.SharedPreferences
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.util.Util
import io.github.vladimirmi.internetradioplayer.data.manager.BUFFER_LENGTH_KEY
import io.github.vladimirmi.internetradioplayer.data.manager.INITIAL_BUFFER_LENGTH_KEY
import io.github.vladimirmi.internetradioplayer.data.manager.Preferences
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 22.09.2018.
 */

class LoadControl
@Inject constructor(private val prefs: Preferences)
    : DefaultLoadControl(DefaultAllocator(true, C.DEFAULT_AUDIO_BUFFER_SIZE)) {

    private var targetBufferSize = 0
    private var initialBufferUs = prefs.initialBufferLength * 1000000L
    private var bufferUs = prefs.bufferLength * 1000000L
    private val minBufferUs = DEFAULT_MIN_BUFFER_MS * 1000L
    private val maxBufferUs = DEFAULT_MAX_BUFFER_MS * 1000L
    private var isBuffering: Boolean = false


    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == INITIAL_BUFFER_LENGTH_KEY) {
            initialBufferUs = prefs.initialBufferLength * 1000000L
            reset(true)

        } else if (key == BUFFER_LENGTH_KEY) {
            bufferUs = prefs.bufferLength * 1000000L
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
