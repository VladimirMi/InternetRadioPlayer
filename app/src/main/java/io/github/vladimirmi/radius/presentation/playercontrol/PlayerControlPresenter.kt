package io.github.vladimirmi.radius.presentation.playercontrol

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.ioToMain
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.model.repository.MediaController
import io.github.vladimirmi.radius.model.service.AvailableActions
import io.github.vladimirmi.radius.model.service.PlayerService
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

@InjectViewState
class PlayerControlPresenter
@Inject constructor(private val mediaController: MediaController,
                    private val stationInteractor: StationInteractor,
                    private val router: Router)
    : BasePresenter<PlayerControlView>() {

    override fun onFirstViewAttach() {
        mediaController.playbackState
                .subscribe { handleState(it) }
                .addTo(compDisp)

        stationInteractor.currentStationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { viewState.setStation(it) }
                .addTo(compDisp)

        mediaController.sessionEvent
                .subscribe { handleSessionEvent(it) }
                .addTo(compDisp)

        stationInteractor.currentIconObs
                .ioToMain()
                .subscribe { viewState.setStationIcon(it.bitmap) }
                .addTo(compDisp)
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            STATE_PAUSED, STATE_STOPPED -> viewState.showStopped()
            STATE_PLAYING -> viewState.showPlaying()
        }
        if (AvailableActions.isNextPreviousEnabled(state.actions)) {
            viewState.enableNextPrevious(true)
        } else {
            viewState.enableNextPrevious(false)
        }
    }

    private fun handleSessionEvent(event: String) {
        when (event) {
            PlayerService.EVENT_SESSION_PREVIOUS -> router.skipToPrevious(stationInteractor.currentStation)
            PlayerService.EVENT_SESSION_NEXT -> router.skipToNext(stationInteractor.currentStation)
        }
    }

    fun playPause() {
        with(mediaController) {
            if (!isPlaying && !isNetAvail) {
                viewState.showToast(R.string.toast_net_error)
            } else {
                playPause()
            }
        }
    }

    fun switchFavorite() {
        val current = stationInteractor.currentStation
        val copy = current.copy(favorite = !current.favorite)
        stationInteractor.updateCurrentStation(copy)
                .subscribe()
                .addTo(compDisp)
    }

    fun showStation() {
        router.showStationSlide(stationInteractor.currentStation)
    }

    fun skipPrevious() {
        mediaController.skipToPrevious()
    }

    fun skipNext() {
        mediaController.skipToNext()
    }
}