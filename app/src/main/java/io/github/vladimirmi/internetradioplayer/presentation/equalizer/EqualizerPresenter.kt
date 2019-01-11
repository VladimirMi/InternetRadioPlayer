package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import android.media.audiofx.Equalizer
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerPresenter
@Inject constructor() : BasePresenter<EqualizerView>() {

    override fun onFirstAttach(view: EqualizerView) {

        val equalizer = Equalizer(1, 66)
        Timber.e("onAudioSessionId: settings - ${equalizer.properties}")
        val bands = (0 until equalizer.numberOfBands)
                .map { (equalizer.getCenterFreq(it.toShort()) / 1000).toString() }
        val values = (0 until equalizer.numberOfBands)
                .map { equalizer.getBandLevel(it.toShort()).toInt() }
        val range = equalizer.bandLevelRange
        view.setBands(bands, values, range[0].toInt(), range[1].toInt())

        Timber.e("onAudioSessionId: bands - $bands")
        Timber.e("onAudioSessionId: presets - ${(0 until equalizer.numberOfPresets)
                .joinToString { equalizer.getPresetName(it.toShort()) }}")
        Timber.e("onAudioSessionId: band level range - ${Arrays.toString(equalizer.bandLevelRange)}")
    }
}