package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerPresenter
@Inject constructor() : BasePresenter<EqualizerView>() {

    override fun onFirstAttach(view: EqualizerView) {
        view.setBands(listOf("0", "1", "2", "3", "4"),
                listOf(12, 15, 4, 6, 89), -100, 100)
    }
}