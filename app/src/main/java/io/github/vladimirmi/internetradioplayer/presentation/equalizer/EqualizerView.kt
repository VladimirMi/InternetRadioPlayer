package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

interface EqualizerView : BaseView {

    fun setBands(bands: List<String>, values: List<Int>, min: Int, max: Int)
}