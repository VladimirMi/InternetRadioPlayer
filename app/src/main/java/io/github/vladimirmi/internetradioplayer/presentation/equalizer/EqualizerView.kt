package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

interface EqualizerView : BaseView {

    fun setupBands(bands: List<String>, min: Int, max: Int)

    fun setBandLevels(bandLevels: List<Int>)

    fun setBassBoost(bassBoost: Int)

    fun setVirtualizer(virtualizer: Int)

    fun setPresets(presets: List<String>, curPreset: Int)
}