package io.github.vladimirmi.internetradioplayer.presentation.playercontrol

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.*
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.PlayerMode
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class PlayerControlPresenter
@Inject constructor(private val controlsInteractor: PlayerControlsInteractor,
                    private val stationInteractor: StationInteractor,
                    private val router: Router)
    : BasePresenter<PlayerControlView>() {

    override fun onFirstAttach(view: PlayerControlView) {
        controlsInteractor.playbackStateObs
                .subscribe { handleState(it) }
                .addTo(viewSubs)

        controlsInteractor.sessionEventObs
                .subscribe { handleSessionEvent(it) }
                .addTo(viewSubs)

        controlsInteractor.playbackMetaData
                .subscribeBy { handleMetadata(it) }
                .addTo(viewSubs)

        controlsInteractor.playerModeObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { handlePlayerMode(it) }
                .addTo(viewSubs)

        stationInteractor.currentStationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { view.setStation(it) }
                .addTo(viewSubs)
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            STATE_PAUSED, STATE_STOPPED -> view?.showStopped()
            STATE_BUFFERING -> view?.showLoading()
            STATE_PLAYING -> view?.showPlaying()
        }
    }

    private fun handleMetadata(metadata: MediaMetadataCompat) {
        //todo fix
//        if (metadata.notSupported()&&metadata.notEmpty()) viewState.setMetadata(metadata.album!!)
        if (metadata.notSupported() && metadata.album != null) view?.setMetadata(metadata.album!!)
        else view?.setMetadata("${metadata.artist} - ${metadata.title}")
    }

    private fun handleSessionEvent(event: String) {
        when (event) {
            PlayerService.EVENT_SESSION_PREVIOUS -> {
                view?.showPrevious()
                router.skipToPrevious(stationInteractor.currentStation.id)
            }
            PlayerService.EVENT_SESSION_NEXT -> {
                view?.showNext()
                router.skipToNext(stationInteractor.currentStation.id)
            }
        }
    }

    private fun handlePlayerMode(mode: PlayerMode) {
        when (mode) {
            PlayerMode.NORMAL_MODE -> view?.enableEditMode(false)
            PlayerMode.EDIT_MODE -> view?.enableEditMode(true)
        }
    }

    fun playPause() {
        with(controlsInteractor) {
            if (!isPlaying && !isNetAvail) {
                view?.showToast(R.string.toast_net_error)
            } else {
                playPause()
            }
        }
    }

    fun showStation() {
        router.showStationSlide(stationInteractor.currentStation.id)
    }

    fun skipToPrevious() {
        controlsInteractor.skipToPrevious()
    }

    fun skipToNext() {
        controlsInteractor.skipToNext()
    }

    fun changeIcon() {
        router.navigateTo(Router.ICON_PICKER_SCREEN)
    }
}
