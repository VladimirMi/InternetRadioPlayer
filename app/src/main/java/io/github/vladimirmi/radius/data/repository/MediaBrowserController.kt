package io.github.vladimirmi.radius.data.repository

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.radius.data.service.PlayerService
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.10.2017.
 */


class MediaBrowserController
@Inject constructor(context: Context, private val mediaRepository: MediaRepository) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var controller: MediaControllerCompat? = null
    private val callbacks: HashSet<MediaControllerCompat.Callback> = HashSet()

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Timber.d("onConnected: session token ${mediaBrowser.sessionToken.token}")
            try {
                controller = MediaControllerCompat(context, mediaBrowser.sessionToken)
            } catch (e: RemoteException) {
                Timber.e(e, e.localizedMessage)
            }
            callbacks.forEach { controller?.registerCallback(it) }
        }

        override fun onConnectionSuspended() {
            Timber.d("onConnectionSuspended")
            callbacks.forEach { controller?.unregisterCallback(it) }
            controller = null
        }

        override fun onConnectionFailed() {
            Timber.d("onConnectionFailed")
        }
    }

    init {
        mediaBrowser = MediaBrowserCompat(context,
                ComponentName(context, PlayerService::class.java),
                connectionCallbacks, null)
    }

    fun connect() {
        mediaBrowser.connect()
    }

    fun disconnect() {
        mediaBrowser.disconnect()
    }

    fun registerCallback(callback: MediaControllerCompat.Callback) {
        controller?.registerCallback(callback)
        callbacks.add(callback)
    }

    fun unRegisterCallback(callback: MediaControllerCompat.Callback) {
        controller?.unregisterCallback(callback)
        callbacks.remove(callback)
    }

    fun isPlaying(uri: Uri) = controller?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING
            && mediaRepository.currentMedia()?.uri == uri

    fun play(uri: Uri) {
        controller?.transportControls?.playFromUri(uri, null)
    }

    fun pause() {
        controller?.transportControls?.pause()
    }

    fun stop() {
        controller?.transportControls?.stop()
    }
}