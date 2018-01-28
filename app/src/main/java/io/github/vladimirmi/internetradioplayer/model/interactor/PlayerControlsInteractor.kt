package io.github.vladimirmi.internetradioplayer.model.interactor

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.internetradioplayer.model.entity.PlayerMode
import io.github.vladimirmi.internetradioplayer.model.manager.NetworkChecker
import io.github.vladimirmi.internetradioplayer.model.repository.MediaController
import io.github.vladimirmi.internetradioplayer.model.repository.StationListRepository
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class PlayerControlsInteractor
@Inject constructor(private val repository: StationListRepository,
                    private val controller: MediaController,
                    private val networkChecker: NetworkChecker) {

    private var enableNextPreviousListener: ((Boolean) -> Unit)? = null

    private val enableNextPreviousObs = Observable.create<PlayerMode> { e ->
        enableNextPreviousListener = { enabled ->
            if (!e.isDisposed) {
                if (enabled) e.onNext(PlayerMode.NEXT_PREVIOUS_ENABLED)
                else e.onNext(PlayerMode.NEXT_PREVIOUS_DISABLED)
            }
        }
        e.setDisposable(Disposables.fromRunnable { enableNextPreviousListener = null })
    }

    val playerModeObs: Observable<PlayerMode> = repository.stationList.observe()
            .map {
                if (it.itemsSize > 1) {
                    PlayerMode.NEXT_PREVIOUS_ENABLED
                } else {
                    PlayerMode.NEXT_PREVIOUS_DISABLED
                }
            }.mergeWith(enableNextPreviousObs)

    val playbackState: Observable<PlaybackStateCompat> = controller.playbackState
    val playbackMetaData: Observable<MediaMetadataCompat> = controller.playbackMetaData
    val sessionEvent: Observable<String> = controller.sessionEvent

    fun tryEnableNextPrevious(enable: Boolean) {
        enableNextPreviousListener?.invoke(enable && repository.stationList.itemsSize > 1)
    }

    fun nextStation(cycle: Boolean = true): Boolean {
        val next = repository.stationList.getNext(repository.currentStation.value, cycle)
        return if (next != null) {
            repository.setCurrentStation(next)
            true
        } else false
    }

    fun previousStation(cycle: Boolean = true): Boolean {
        val previous = repository.stationList.getPrevious(repository.currentStation.value, cycle)
        return if (previous != null) {
            repository.setCurrentStation(previous)
            true
        } else false
    }

    val isPlaying: Boolean
        get() = with(controller.playbackState) {
            hasValue() && (value.state == PlaybackStateCompat.STATE_PLAYING ||
                    value.state == PlaybackStateCompat.STATE_BUFFERING)
        }

    val isStopped: Boolean
        get() = with(controller.playbackState) {
            hasValue() && (value.state == PlaybackStateCompat.STATE_STOPPED)
        }

    val isNetAvail: Boolean get() = networkChecker.isAvailable()

    fun connect() = controller.connect()

    fun disconnect() = controller.disconnect()

    fun playPause() {
        if (isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun stop() = controller.stop()

    fun skipToPrevious() = controller.skipToPrevious()

    fun skipToNext() = controller.skipToNext()
}