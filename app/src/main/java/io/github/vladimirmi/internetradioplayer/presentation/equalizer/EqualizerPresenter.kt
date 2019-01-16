package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import io.github.vladimirmi.internetradioplayer.domain.interactor.EqualizerInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.EqualizerPreset
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerPresenter
@Inject constructor(private val equalizerInteractor: EqualizerInteractor) : BasePresenter<EqualizerView>() {

    override fun onFirstAttach(view: EqualizerView) {
        equalizerInteractor.currentPresetObs
                .distinctUntilChanged(EqualizerPreset::name)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    view.setPresetNames(equalizerInteractor.getPresetNames())
                    view.setPreset(it)
                })
                .addTo(viewSubs)

        view.setupEqualizer(equalizerInteractor.equalizerConfig)
    }

    fun setBandLevel(band: Int, level: Int) {
        equalizerInteractor.setBandLevel(band, level)
    }

    fun setBassBoost(strength: Int) {
        equalizerInteractor.setBassBoostStrength(strength)
    }

    fun setVirtualizer(strength: Int) {
        equalizerInteractor.setVirtualizerStrength(strength)
    }

    fun selectPreset(index: Int) {
        equalizerInteractor.selectPreset(index)
    }

    fun saveCurrentPreset() {
        equalizerInteractor.saveCurrentPreset()
                .subscribeX()
                .addTo(dataSubs)
    }
}