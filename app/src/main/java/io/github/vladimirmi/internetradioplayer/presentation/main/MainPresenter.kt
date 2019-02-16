package io.github.vladimirmi.internetradioplayer.presentation.main

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.service.*
import io.github.vladimirmi.internetradioplayer.domain.interactor.MainInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainPresenter
@Inject constructor(private val router: Router,
                    private val mainInteractor: MainInteractor,
                    private val playerInteractor: PlayerInteractor)
    : BasePresenter<MainView>() {

    override fun onAttach(view: MainView) {
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

    fun selectPage(position: Int) {
        val pageId = when (position) {
            0 -> R.id.nav_search
            1 -> R.id.nav_favorites
            2 -> R.id.nav_player
            else -> R.id.nav_history
        }
        mainInteractor.saveMainPageId(pageId)
        router.replaceScreen(pageId)
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

    private fun handleSessionEvent(event: Pair<String, Bundle>) {
//        when (event) {
//            PlayerService.EVENT_SESSION_PREVIOUS -> view?.showPrevious()
//            PlayerService.EVENT_SESSION_NEXT -> view?.showNext()
//        }
    }
}
