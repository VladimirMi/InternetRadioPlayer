package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import io.github.vladimirmi.internetradioplayer.data.repository.EqualizerRepository
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerPresenter
@Inject constructor(private val equalizerRepository: EqualizerRepository) : BasePresenter<EqualizerView>() {

    override fun onFirstAttach(view: EqualizerView) {

        val bands = equalizerRepository.bands
        val values = bands.map { it.length }
        val range = equalizerRepository.levelRange
        view.setBands(bands, values, range.first, range.second)

        equalizerRepository.equalizerObs.subscribeX()
                .addTo(dataSubs)
    }

    fun setBandLevel(band: Int, level: Int) {
        equalizerRepository.setBandLevel(band, level)
    }

    fun setBassBoost(strength: Int) {
        equalizerRepository.setBassBoost(strength)
    }

    fun setVirtualizer(strength: Int) {
        equalizerRepository.setVirtualizer(strength)
    }
}