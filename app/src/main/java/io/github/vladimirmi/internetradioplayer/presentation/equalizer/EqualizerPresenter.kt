package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import io.github.vladimirmi.internetradioplayer.domain.interactor.EqualizerInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerPresenter
@Inject constructor(private val equalizerInteractor: EqualizerInteractor)
    : BasePresenter<EqualizerView>() {

    override fun onFirstAttach(view: EqualizerView) {
        view.setupEqualizer(equalizerInteractor.equalizerConfig)
    }

    override fun onAttach(view: EqualizerView) {
        equalizerInteractor.currentPresetObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    view.setPresetNames(equalizerInteractor.getPresetNames())
                    view.setBindIcon(equalizerInteractor.presetBinder.iconResId)
                    view.setPreset(it)
                    view.showReset(equalizerInteractor.isCurrentPresetCanReset())
                })
                .addTo(viewSubs)

        equalizerInteractor.equalizerEnabledObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.enableEqualizer(it) })
                .addTo(viewSubs)
    }

    override fun onDestroy() {
        equalizerInteractor.bindPreset()
                .subscribeX()
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

    fun switchBind() {
        equalizerInteractor.switchBind()
        view?.setBindIcon(equalizerInteractor.presetBinder.iconResId)
        view?.showToast(equalizerInteractor.presetBinder.descriptionResId)
    }

    fun resetCurrentPreset() {
        equalizerInteractor.resetCurrentPreset()
                .subscribeX()
                .addTo(dataSubs)
    }

    fun enableEqualizer(enabled: Boolean) {
        Timber.e("enableEqualizer: ")
        equalizerInteractor.enableEqualizer(enabled)
    }
}