package io.github.vladimirmi.internetradioplayer.presentation.playercontrol

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.*
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.PlayerMode
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
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

        controlsInteractor.playbackMetaData
                .subscribeBy { handleMetadata(it) }
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
            STATE_BUFFERING -> viewState.showLoading()
            STATE_PLAYING -> viewState.showPlaying()
        }
    }

    private fun handleMetadata(metadata: MediaMetadataCompat) {
        //todo fix
//        if (metadata.notSupported()&&metadata.notEmpty()) viewState.setMetadata(metadata.album!!)
        if (metadata.notSupported() && metadata.album != null) viewState.setMetadata(metadata.album!!)
        else viewState.setMetadata("${metadata.artist} - ${metadata.title}")
    }

    private fun handleSessionEvent(event: String) {
        when (event) {
            PlayerService.EVENT_SESSION_PREVIOUS -> {
                viewState.showPrevious()
                router.skipToPrevious(stationInteractor.currentStation.id)
            }
            PlayerService.EVENT_SESSION_NEXT -> {
                viewState.showNext()
                router.skipToNext(stationInteractor.currentStation.id)
            }
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
