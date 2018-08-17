package io.github.vladimirmi.internetradioplayer.model.interactor

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.model.entity.PlayerMode
import io.github.vladimirmi.internetradioplayer.model.manager.NetworkChecker
import io.github.vladimirmi.internetradioplayer.model.repository.MediaController
import io.github.vladimirmi.internetradioplayer.model.repository.StationListRepository
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class PlayerControlsInteractor
@Inject constructor(private val repository: StationListRepository,
                    private val controller: MediaController,
                    private val networkChecker: NetworkChecker) {

    //todo playerMode relay
    private var oneStation = true
    private val playerMode = BehaviorRelay.createDefault(PlayerMode.NEXT_PREVIOUS_ENABLED)
    val playbackStateObs: Observable<PlaybackStateCompat> get() = controller.playbackState
    //todo to Metadata
    val playbackMetaData: Observable<MediaMetadataCompat> get() = controller.playbackMetaData
    val sessionEventObs: Observable<String> get() = controller.sessionEvent
    val playerModeObs: Observable<PlayerMode> get() = playerMode

    init {
        repository.stationList.observe()
                .map {
                    if (it.itemsSize > 1) {
                        oneStation = false
                        PlayerMode.NEXT_PREVIOUS_ENABLED
                    } else {
                        oneStation = true
                        PlayerMode.NEXT_PREVIOUS_DISABLED
                    }
                }.distinctUntilChanged()
                .subscribe(playerMode::accept)
    }

    fun editMode(enable: Boolean) {
        playerMode.accept((if (enable) PlayerMode.EDIT_MODE else PlayerMode.NORMAL_MODE))
        if (!oneStation) {
            playerMode.accept((if (enable) PlayerMode.NEXT_PREVIOUS_DISABLED else PlayerMode.NEXT_PREVIOUS_ENABLED))
        }
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
