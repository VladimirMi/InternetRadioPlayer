package io.github.vladimirmi.internetradioplayer.domain.interactor

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.repository.MediaController
import io.github.vladimirmi.internetradioplayer.data.repository.StationListRepository
import io.github.vladimirmi.internetradioplayer.data.utils.NetworkChecker
import io.github.vladimirmi.internetradioplayer.domain.model.PlayerMode
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class PlayerControlsInteractor
@Inject constructor(private val repository: StationListRepository,
                    private val controller: MediaController,
                    private val networkChecker: NetworkChecker) {

    private val playerMode = BehaviorRelay.createDefault(PlayerMode.NORMAL_MODE)
    val playbackStateObs: Observable<PlaybackStateCompat> get() = controller.playbackState
    val playbackMetaData: Observable<MediaMetadataCompat> get() = controller.playbackMetaData
    val sessionEventObs: Observable<String> get() = controller.sessionEvent
    val playerModeObs: Observable<PlayerMode> get() = playerMode

    fun editMode(enable: Boolean) {
        playerMode.accept((if (enable) PlayerMode.EDIT_MODE else PlayerMode.NORMAL_MODE))
    }

    val isPlaying: Boolean
        get() = with(controller.playbackState) {
            hasValue() && (value?.state == PlaybackStateCompat.STATE_PLAYING ||
                    value?.state == PlaybackStateCompat.STATE_BUFFERING)
        }

    val isStopped: Boolean
        get() = with(controller.playbackState) {
            hasValue() && (value?.state == PlaybackStateCompat.STATE_STOPPED)
        }

    val isNetAvail: Boolean get() = networkChecker.isAvailable()

    fun connect() = controller.connect()

    fun disconnect() = controller.disconnect()

    fun play() = controller.play()

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
