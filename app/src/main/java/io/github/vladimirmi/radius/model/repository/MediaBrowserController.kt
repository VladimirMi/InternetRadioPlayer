package io.github.vladimirmi.radius.model.repository

import android.content.ComponentName
import android.content.Context
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.radius.model.service.PlayerService
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.10.2017.
 */


class MediaBrowserController
@Inject constructor(context: Context, private val repository: StationRepository) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var controller: MediaControllerCompat? = null

    val playbackState: BehaviorRelay<PlaybackStateCompat> = BehaviorRelay.create()
    val playbackMetaData: BehaviorRelay<MediaMetadataCompat> = BehaviorRelay.create()

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                controller = MediaControllerCompat(context, mediaBrowser.sessionToken)
            } catch (e: RemoteException) {
                Timber.e(e, e.localizedMessage)
            }
            controller?.registerCallback(controllerCallback)
        }

        override fun onConnectionSuspended() {
            Timber.d("onConnectionSuspended")
            controller?.unregisterCallback(controllerCallback)
            controller = null
        }

        override fun onConnectionFailed() {
            Timber.d("onConnectionFailed")
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Timber.e("onPlaybackStateChanged: ${state.state}")
            playbackState.accept(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            playbackMetaData.accept(metadata)
        }
    }

    init {
        mediaBrowser = MediaBrowserCompat(context,
                ComponentName(context, PlayerService::class.java),
                connectionCallbacks, null)
    }

    fun connect() {
        if (mediaBrowser.isConnected) return
        mediaBrowser.connect()
    }

    fun disconnect() {
        mediaBrowser.disconnect()
    }

    val isPlaying: Boolean
        get() = playbackState.hasValue() &&
                (playbackState.value.state == PlaybackStateCompat.STATE_PLAYING ||
                        playbackState.value.state == PlaybackStateCompat.STATE_BUFFERING)


    fun playPause() {
        if (isPlaying) {
            controller?.transportControls?.pause()
        } else {
            controller?.transportControls?.play()
        }
    }

    fun skipToPrevious() {
        controller?.transportControls?.skipToPrevious()
    }

    fun skipToNext() {
        controller?.transportControls?.skipToNext()
    }
}