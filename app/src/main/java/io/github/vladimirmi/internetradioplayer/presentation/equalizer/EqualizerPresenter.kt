package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import io.github.vladimirmi.internetradioplayer.domain.interactor.EqualizerInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerPresenter
@Inject constructor(private val equalizerInteractor: EqualizerInteractor) : BasePresenter<EqualizerView>() {

    override fun onFirstAttach(view: EqualizerView) {

        val bands = equalizerInteractor.bands
        val range = equalizerInteractor.levelRange
        view.setupBands(bands, range.first, range.second)
        view.setBandLevels(equalizerInteractor.bandLevels)
        view.setBassBoost(equalizerInteractor.bassBoost)
        view.setVirtualizer(equalizerInteractor.virtualizer)

        //todo move to player service
        equalizerInteractor.equalizerInit.subscribeX()
                .addTo(dataSubs)

        Singles.zip(equalizerInteractor.getPresets(), equalizerInteractor.getCurrentPreset()) { list, preset ->
            list to list.indexOf(preset)
        }.subscribeX(onSuccess = { view.setPresets(it.first, it.second) })
                .addTo(viewSubs)

    }

    fun setBandLevel(band: Int, level: Int) {
        equalizerInteractor.setBandLevel(band, level)
    }

    fun setBassBoost(strength: Int) {
        equalizerInteractor.bassBoost = strength
    }

    fun setVirtualizer(strength: Int) {
        equalizerInteractor.virtualizer = strength
    }

    fun selectPreset(preset: String) {
        equalizerInteractor.selectPreset(preset)
        view?.setBandLevels(equalizerInteractor.bandLevels)
    }
}