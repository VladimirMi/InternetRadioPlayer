package io.github.vladimirmi.internetradioplayer.data.service

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.upstream.DefaultAllocator

/**
 * Created by Vladimir Mikhalev 22.09.2018.
 */

private const val MIN_BUFFER_MS = 30000
private const val MAX_BUFFER_MS = 40000
private const val BUFFER_FOR_PLAYBACK_MS = 4000
private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 6000

class LoadControl : DefaultLoadControl(
        DefaultAllocator(true, C.DEFAULT_AUDIO_BUFFER_SIZE),
        MIN_BUFFER_MS, MAX_BUFFER_MS,
        BUFFER_FOR_PLAYBACK_MS, BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
        -1, true)
