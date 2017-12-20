package io.github.vladimirmi.radius.presentation.playerControl

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.MediaController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.model.service.PlayerService
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

@InjectViewState
class PlayerControlPresenter
@Inject constructor(private val mediaController: MediaController,
                    private val repository: StationRepository,
                    private val router: Router)
    : BasePresenter<PlayerControlView>() {

    override fun onFirstViewAttach() {
        mediaController.playbackState
                .subscribe { handleState(it) }
                .addTo(compDisp)

        repository.currentStation
                .subscribe { handleStation(it) }
                .addTo(compDisp)

        mediaController.sessionEvent
                .subscribe { handleSessionEvent(it) }
                .addTo(compDisp)
    }

    private fun handleState(state: PlaybackStateCompat) {
        Timber.e("handleState: ")
        when (state.state) {
            STATE_PAUSED, STATE_STOPPED -> viewState.showStopped()
            STATE_PLAYING -> viewState.showPlaying()
        }
        if (state.actions and ACTION_SKIP_TO_NEXT == ACTION_SKIP_TO_NEXT) {
            viewState.enableNextPrevious(true)
        } else {
            viewState.enableNextPrevious(false)
        }
    }

    private fun handleStation(it: Station) {
        viewState.setStation(it)
        viewState.setStationIcon(repository.getStationIcon().blockingGet())
    }

    private fun handleSessionEvent(event: String) {
        when (event) {
            PlayerService.EVENT_SESSION_PREVIOUS -> router.skipToPrevious()
            PlayerService.EVENT_SESSION_NEXT -> router.skipToNext()
        }
    }

    fun playPause() {
        mediaController.playPause()
    }

    fun switchFavorite() {
        val current = repository.currentStation.value
        val copy = current.copy(favorite = !current.favorite)
        repository.updateStation(copy)
    }

    fun showStation() {
        router.navigateTo(Router.STATION_SCREEN)
    }

    fun skipPrevious() {
        mediaController.skipToPrevious()
    }

    fun skipNext() {
        mediaController.skipToNext()
    }
}