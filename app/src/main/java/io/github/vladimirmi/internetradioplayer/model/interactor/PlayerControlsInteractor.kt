package io.github.vladimirmi.internetradioplayer.model.interactor

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.internetradioplayer.model.entity.PlayerMode
import io.github.vladimirmi.internetradioplayer.model.manager.NetworkChecker
import io.github.vladimirmi.internetradioplayer.model.repository.MediaController
import io.github.vladimirmi.internetradioplayer.model.repository.StationListRepository
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class PlayerControlsInteractor
@Inject constructor(private val repository: StationListRepository,
                    private val controller: MediaController,
                    private val networkChecker: NetworkChecker) {

    private var enableNextPreviousManualListener: ((Boolean) -> Unit)? = null

    private val enableNextPreviousManualObs = Observable.create<PlayerMode> { e ->
        enableNextPreviousManualListener = { enabled ->
            if (!e.isDisposed) {
                if (enabled) e.onNext(PlayerMode.NEXT_PREVIOUS_ENABLED)
                else e.onNext(PlayerMode.NEXT_PREVIOUS_DISABLED)
            }
        }
        e.setDisposable(Disposables.fromRunnable { enableNextPreviousManualListener = null })
    }
            .distinctUntilChanged()
            .startWith(PlayerMode.NEXT_PREVIOUS_ENABLED)

    private val enableNextPreviousAutoObs = repository.stationList.observe()
            .map {
                if (it.itemsSize > 1) PlayerMode.NEXT_PREVIOUS_ENABLED
                else PlayerMode.NEXT_PREVIOUS_DISABLED
            }.distinctUntilChanged()

    val playerModeObs: Observable<PlayerMode> = Observable.combineLatest(
            enableNextPreviousAutoObs,
            enableNextPreviousManualObs,
            BiFunction { manual: PlayerMode, auto: PlayerMode ->
                if (manual == PlayerMode.NEXT_PREVIOUS_ENABLED && auto == PlayerMode.NEXT_PREVIOUS_ENABLED) {
                    PlayerMode.NEXT_PREVIOUS_ENABLED
                } else {
                    PlayerMode.NEXT_PREVIOUS_DISABLED
                }
            }).replay(1).refCount()

    val playbackStateObs: Observable<PlaybackStateCompat> get() = controller.playbackState
    val playbackMetaData: Observable<MediaMetadataCompat> get() = controller.playbackMetaData
    val sessionEventObs: Observable<String> get() = controller.sessionEvent

    fun enableNextPrevious(enable: Boolean) {
        enableNextPreviousManualListener?.invoke(enable)
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
