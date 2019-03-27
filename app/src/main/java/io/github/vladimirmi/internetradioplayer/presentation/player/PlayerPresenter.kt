package io.github.vladimirmi.internetradioplayer.presentation.player

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.C
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.github.vladimirmi.internetradioplayer.data.service.extensions.*
import io.github.vladimirmi.internetradioplayer.domain.interactor.*
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class PlayerPresenter
@Inject constructor(private val stationInteractor: StationInteractor,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val playerInteractor: PlayerInteractor,
                    private val mediaInteractor: MediaInteractor,
                    private val coverArtInteractor: CoverArtInteractor)
    : BasePresenter<PlayerView>() {

    private var playTask: TimerTask? = null

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
                        view?.setFavorite(favoriteListInteractor.isFavorite(it))
                    } else if (it is Record) {
                        view?.setDuration(it.duration)
                    }
                })
                .addTo(viewSubs)
    }

    private fun setupPlayer() {
        playerInteractor.playbackStateObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { handleState(it) })
                .addTo(viewSubs)

        playerInteractor.metadataObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { handleMetadata(it) })
                .addTo(viewSubs)

        playerInteractor.sessionEventObs
                .observeOn(AndroidSchedulers.mainThread())
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

    fun seekTo(position: Int) {
        playerInteractor.seekTo(position)
    }

    private fun handleState(state: PlaybackStateCompat) {
        playTask?.cancel()
        view?.setPosition(state.position)
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
                playTask = Timer().schedule(0, 300) { view?.incrementPositionBy(300) }
            }
        }
        view?.enableSeek(PlayerActions.isSeekEnabled(state.actions))
        view?.enableSkip(PlayerActions.isSkipEnabled(state.actions))
    }

    private fun handleMetadata(metadata: MediaMetadataCompat) {
        val metadataLine = if (metadata.isEmpty() || metadata.isNotSupported()) metadata.album
        else "${metadata.artist} - ${metadata.title}"
        view?.setMetadata(metadataLine)
        if (metadata.duration != C.TIME_UNSET) view?.setDuration(metadata.duration)
    }

    private fun handleSessionEvent(event: Pair<String, Bundle>) {
        when (event.first) {
            PlayerService.EVENT_SESSION_PREVIOUS -> view?.showPrevious()
            PlayerService.EVENT_SESSION_NEXT -> view?.showNext()
        }
    }

}
