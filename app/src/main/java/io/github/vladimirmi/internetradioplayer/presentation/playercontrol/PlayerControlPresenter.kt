package io.github.vladimirmi.internetradioplayer.presentation.playercontrol

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.*
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class PlayerControlPresenter
@Inject constructor(private val playerInteractor: PlayerInteractor,
                    private val stationInteractor: StationInteractor,
                    private val router: Router)
    : BasePresenter<PlayerControlView>() {

    override fun onFirstAttach(view: PlayerControlView) {
        playerInteractor.playbackStateObs
                .subscribe { handleState(it) }
                .addTo(viewSubs)

//        playerInteractor.sessionEventObs
//                .subscribe { handleSessionEvent(it) }
//                .addTo(viewSubs)

        playerInteractor.playbackMetaData
                .subscribeBy { handleMetadata(it) }
                .addTo(viewSubs)

        stationInteractor.stationObs
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
                Timber.e("handleSessionEvent: previous")
//                router.skipToPrevious(stationInteractor.station.id)
            }
            PlayerService.EVENT_SESSION_NEXT -> {
                view?.showNext()
                Timber.e("handleSessionEvent: next")
//                router.skipToNext(stationInteractor.station.id)
            }
        }
    }

    fun playPause() {
        with(playerInteractor) {
            if (!isPlaying && !isNetAvail) {
                view?.showMessage(R.string.msg_net_error)
            } else {
                playPause()
            }
        }
    }

    fun showStation() {
        Timber.e("showStation: ")
//        router.showStationSlide(stationInteractor.station.id)
    }

    fun skipToPrevious() {
        playerInteractor.skipToPrevious()
    }

    fun skipToNext() {
        playerInteractor.skipToNext()
    }

    fun changeIcon() {
        router.navigateTo(Router.ICON_PICKER_SCREEN)
    }
}
