package io.github.vladimirmi.internetradioplayer.presentation.player

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.RecordsRepository
import io.github.vladimirmi.internetradioplayer.data.service.*
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
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
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class PlayerPresenter
@Inject constructor(private val stationInteractor: StationInteractor,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val playerInteractor: PlayerInteractor,
                    private val recordsRepository: RecordsRepository,
                    private val mediaInteractor: MediaInteractor,
                    private val router: Router)
    : BasePresenter<PlayerView>() {

    override fun onAttach(view: PlayerView) {
        setupStation()
        setupPlayer()
    }

    private fun setupStation() {
        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    view?.setMedia(it)
                    if (it is Station) {
                        view?.setSpecs(it.specs)
                        view?.setFavorite(favoriteListInteractor.isFavorite(it))
                        view?.setGroup(favoriteListInteractor.findGroup(it.groupId)?.name)
                    }
                })
                .addTo(viewSubs)
    }


    private fun setupPlayer() {
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

    fun switchFavorite() {
        val station = mediaInteractor.currentMedia as? Station ?: return
        stationInteractor.switchFavorite(station)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX()
                .addTo(dataSubs)
    }

    fun playPause() {
        with(playerInteractor) {
            if (!isPlaying && !isNetAvail) {
                view?.showSnackbar(R.string.msg_net_error)
            } else {
                playPause()
            }
        }
    }

    fun stop() {
        playerInteractor.stop()
    }

    fun skipToPrevious() {
        playerInteractor.skipToPrevious()
    }

    fun skipToNext() {
        playerInteractor.skipToNext()
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            PlaybackStateCompat.STATE_PAUSED -> {
                view?.showPlaying(false)
                view?.setStatus(R.string.status_paused)
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                view?.showPlaying(false)
                view?.setStatus(R.string.status_stopped)
            }
            PlaybackStateCompat.STATE_BUFFERING -> {
                view?.showPlaying(true)
                view?.setStatus(R.string.metadata_buffering)
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                view?.showPlaying(true)
                view?.setStatus(R.string.status_playing)
            }
        }
    }

    private fun handleMetadata(metadata: MediaMetadataCompat) {
        view?.setMetadata(metadata.artist, metadata.title)
        val metadataLine = if (metadata.isEmpty() || metadata.isNotSupported()) metadata.album
        else "${metadata.artist} - ${metadata.title}"
        view?.setSimpleMetadata(metadataLine)
    }

    private fun handleSessionEvent(event: Pair<String, Bundle>) {
        when (event.first) {
            PlayerService.EVENT_SESSION_PREVIOUS -> view?.showPrevious()
            PlayerService.EVENT_SESSION_NEXT -> view?.showNext()
        }
    }

    fun openEqualizer() {
        router.navigateTo(R.id.nav_equalizer)
    }

    fun scheduleRecord() {
        recordsRepository.startCurrentRecord()
    }
}
