package io.github.vladimirmi.radius.model.repository

import android.arch.lifecycle.MutableLiveData
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.radius.model.service.PlayerService
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.10.2017.
 */


class MediaBrowserController
@Inject constructor(context: Context,
                    private val mediaRepository: MediaRepository) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var controller: MediaControllerCompat? = null
    val playbackState: MutableLiveData<PlaybackStateCompat> = MutableLiveData()
    val playbackMetaData: MutableLiveData<MediaMetadataCompat> = MutableLiveData()

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Timber.d("onConnected: session token ${mediaBrowser.sessionToken.token}")
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
            playbackState.value = state
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            playbackMetaData.value = metadata
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

    fun isPlaying(uri: Uri) = controller?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING
            && mediaRepository.getSelected()?.uri == uri

    fun play(uri: Uri) {
        if (isPlaying(uri)) return
        controller?.transportControls?.playFromUri(uri, null)
    }

    fun pause() {
        controller?.transportControls?.pause()
    }

    fun stop() {
        controller?.transportControls?.stop()
    }
}