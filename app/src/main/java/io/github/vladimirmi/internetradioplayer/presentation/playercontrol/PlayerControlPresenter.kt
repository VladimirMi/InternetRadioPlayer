package io.github.vladimirmi.internetradioplayer.presentation.playercontrol

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.model.entity.PlayerMode
import io.github.vladimirmi.internetradioplayer.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.model.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.model.service.PlayerService
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

@InjectViewState
class PlayerControlPresenter
@Inject constructor(private val controlsInteractor: PlayerControlsInteractor,
                    private val stationInteractor: StationInteractor,
                    private val router: Router)
    : BasePresenter<PlayerControlView>() {

    override fun onFirstViewAttach() {
        controlsInteractor.playbackStateObs
                .subscribe { handleState(it) }
                .addTo(compDisp)

        controlsInteractor.sessionEventObs
                .subscribe { handleSessionEvent(it) }
                .addTo(compDisp)

        controlsInteractor.playerModeObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { handlePlayerMode(it) }
                .addTo(compDisp)

        stationInteractor.currentStationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { viewState.setStation(it) }
                .addTo(compDisp)
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            STATE_PAUSED, STATE_STOPPED -> viewState.showStopped()
            STATE_PLAYING -> viewState.showPlaying()
        }
    }

    private fun handleSessionEvent(event: String) {
        when (event) {
            PlayerService.EVENT_SESSION_PREVIOUS -> router.skipToPrevious(stationInteractor.currentStation.id)
            PlayerService.EVENT_SESSION_NEXT -> router.skipToNext(stationInteractor.currentStation.id)
        }
    }

    private fun handlePlayerMode(mode: PlayerMode) {
        when (mode) {
            PlayerMode.NORMAL_MODE -> viewState.enableEditMode(false)
            PlayerMode.EDIT_MODE -> viewState.enableEditMode(true)
        }
    }

    fun playPause() {
        with(controlsInteractor) {
            if (!isPlaying && !isNetAvail) {
                viewState.showToast(R.string.toast_net_error)
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
