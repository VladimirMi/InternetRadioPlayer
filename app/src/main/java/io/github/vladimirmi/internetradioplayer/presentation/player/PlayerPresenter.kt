package io.github.vladimirmi.internetradioplayer.presentation.player

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.*
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class PlayerPresenter
@Inject constructor(private val stationInteractor: StationInteractor,
                    private val playerInteractor: PlayerInteractor,
                    private val router: Router)
    : BasePresenter<PlayerView>() {

    override fun onAttach(view: PlayerView) {
        stationInteractor.stationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.setStation(it) })
                .addTo(viewSubs)

        playerInteractor.playbackStateObs
                .subscribeX(onNext = { handleState(it) })
                .addTo(viewSubs)


        playerInteractor.metadataObs
                .subscribeX(onNext = { handleMetadata(it) })
                .addTo(viewSubs)

        playerInteractor.sessionEventObs
                .subscribeX(onNext = { handleSessionEvent(it) })
                .addTo(viewSubs)
    }

    fun removeStation() {
//        stationInteractor.removeStation(stationInteractor.currentStation.id)
//                .ioToMain()
//                .subscribeX(onComplete = {
//                    playerInteractor.stop()
//                    Timber.e("removeStation: ")
////                    if (interactor.haveStations()) router.exit()
////                    else router.newRootScreen(Router.GET_STARTED_SCREEN)
//                })
//                .addTo(dataSubs)
    }

    fun edit(stationInfo: StationInfo) {
//        stationInteractor.updateCurrentStation(stationInfo.stationName, stationInfo.groupName)
//                .ioToMain()
//                .subscribeX(onComplete = {})
//                .addTo(viewSubs)
    }

    fun cancelEdit() {
//        stationInteractor.currentStation = stationInteractor.previousWhenEdit!!
//        view?.setStation(stationInteractor.currentStation)
    }

    fun create(stationInfo: StationInfo) {
//        stationInteractor.addCurrentStation(stationInfo.stationName, stationInfo.groupName)
//                .ioToMain()
//                .subscribeX(
//                        onComplete = {
//                            view?.showMessage(R.string.msg_add_success)
//                            Timber.e("create: ${stationInfo.stationName}")
////                            router.newRootScreen(Router.STATIONS_LIST_SCREEN)
//                        })
//                .addTo(viewSubs)
    }

    fun cancelCreate() {
//        stationInteractor.currentStation = stationInteractor.previousWhenEdit!!
//        router.exit()
    }

    fun addShortcut(startPlay: Boolean) {
        if (stationInteractor.addCurrentShortcut(startPlay)) {
            view?.showMessage(R.string.msg_add_shortcut_success)
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

    fun skipToPrevious() {
        playerInteractor.skipToPrevious()
    }

    fun skipToNext() {
        playerInteractor.skipToNext()
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> view?.showStopped()
            PlaybackStateCompat.STATE_BUFFERING -> view?.showBuffering()
            PlaybackStateCompat.STATE_PLAYING -> view?.showPlaying()
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
            PlayerService.EVENT_SESSION_PREVIOUS -> view?.showPrevious()
            PlayerService.EVENT_SESSION_NEXT -> view?.showNext()
        }
    }
}
