package io.github.vladimirmi.internetradioplayer.model.repository

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.model.service.Metadata
import io.github.vladimirmi.internetradioplayer.model.service.PlayerService
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.10.2017.
 */

class MediaController
@Inject constructor(context: Context) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var controller: MediaControllerCompat? = null

    val playbackState: BehaviorRelay<PlaybackStateCompat> = BehaviorRelay.create()
    val playbackMetaData: BehaviorRelay<MediaMetadataCompat> =
            BehaviorRelay.createDefault(Metadata.UNSUPPORTED.toMediaMetadata())
    val sessionEvent: BehaviorRelay<String> = BehaviorRelay.create()

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                controller = MediaControllerCompat(context, mediaBrowser.sessionToken)
            } catch (e: RemoteException) {
                Timber.e(e, e.message)
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
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let {
                playbackState.accept(it)
                if (it.state == PlaybackStateCompat.STATE_PLAYING
                        && Metadata.create(playbackMetaData.value).isSupported) {
                    playbackMetaData.accept(playbackMetaData.value ?: return)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let { playbackMetaData.accept(it) }
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            event?.let { sessionEvent.accept(it) }
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

    fun play() {
        controller?.transportControls?.play()
    }

    fun pause() {
        controller?.transportControls?.pause()
    }

    fun stop() {
        controller?.transportControls?.stop()
    }

    fun skipToPrevious() {
        controller?.transportControls?.skipToPrevious()
    }

    fun skipToNext() {
        controller?.transportControls?.skipToNext()
    }
}
