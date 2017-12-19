package io.github.vladimirmi.radius.presentation.playerControl

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

@InjectViewState
class PlayerControlPresenter
@Inject constructor(private val browserController: MediaBrowserController,
                    private val repository: StationRepository,
                    private val router: Router)
    : BasePresenter<PlayerControlView>() {

    private var skipPrevious = false
    private var skipNext = false

    override fun onFirstViewAttach() {
        //todo turn off next/prev also on the edit mode

        browserController.playbackState
                .subscribeBy { this.handleState(it) }
                .addTo(compDisp)

        repository.currentStation
                .subscribeBy { onStationUpdate(it) }
                .addTo(compDisp)
    }

    private fun onStationUpdate(it: Station) {
        viewState.setStation(it)
        viewState.setStationIcon(repository.getStationIcon().blockingGet())
        if (repository.newStation == it) {
            viewState.createMode(true)
        } else {
            viewState.createMode(false)
        }
        if (skipPrevious) {
            router.skipToPrevious()
            skipPrevious = false
        }
        if (skipNext) {
            skipNext = false
            router.skipToNext()
        }
    }

    private fun handleState(state: PlaybackStateCompat?) {
        when (state?.state) {
            STATE_PAUSED, STATE_STOPPED -> viewState.showStopped()
            STATE_PLAYING -> viewState.showPlaying()
        }
    }

    fun playPause() {
        browserController.playPause()
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
        browserController.skipToPrevious()
        skipPrevious = true
    }

    fun skipNext() {
        browserController.skipToNext()
        skipNext = true
    }
}