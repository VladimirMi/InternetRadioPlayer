package io.github.vladimirmi.internetradioplayer.presentation.equalizer

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.*
import io.github.vladimirmi.internetradioplayer.domain.interactor.EqualizerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.01.2019.
 */

class EqualizerPresenter
@Inject constructor(private val equalizerInteractor: EqualizerInteractor,
                    private val playerInteractor: PlayerInteractor) : BasePresenter<EqualizerView>() {

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

        //todo refactor duplicate code with MainPresenter
        playerInteractor.playbackStateObs
                .subscribeX(onNext = { handleState(it) })
                .addTo(viewSubs)

        playerInteractor.metadataObs
                .subscribeX(onNext = { handleMetadata(it) })
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

    //todo refactor duplicate code (this and PLayerPresenter)
    fun playPause() {
        with(playerInteractor) {
            if (!isPlaying && !isNetAvail) {
                view?.showSnackbar(R.string.msg_net_error)
            } else {
                playPause()
            }
        }
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> view?.showStopped()
            PlaybackStateCompat.STATE_BUFFERING -> view?.showBuffering()
            PlaybackStateCompat.STATE_PLAYING -> view?.showPlaying()
        }
    }

    private fun handleMetadata(metadata: MediaMetadataCompat) {
        when {
            metadata.isNotSupported() -> view?.setMetadata("${metadata.album} - ${metadata.title}")
            metadata.isEmpty() -> view?.setMetadata("${metadata.album}")
            else -> view?.setMetadata("${metadata.artist} - ${metadata.title}")
        }
    }
}