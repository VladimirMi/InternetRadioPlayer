package io.github.vladimirmi.radius.service

import android.app.Activity
import android.content.ComponentName
import android.net.Uri
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.10.2017.
 */


class MediaBrowserController
@Inject constructor(activity: Activity) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null
    private var playbackState = PlaybackStateCompat.STATE_NONE
    private var playingUri :Uri? = null

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Timber.d("onConnected: session token ${mediaBrowser.sessionToken.token}")

            try {
                mediaController = MediaControllerCompat(activity, mediaBrowser.sessionToken)
            } catch (e: RemoteException) {
                Timber.e(e, e.localizedMessage)
            }

            MediaControllerCompat.setMediaController(activity, mediaController)
            mediaController?.registerCallback(controllerCallback)
            transportControls = mediaController?.transportControls
        }

        override fun onConnectionSuspended() {
            Timber.d("onConnectionSuspended")
            mediaController?.unregisterCallback(controllerCallback)
            mediaController = null
            MediaControllerCompat.setMediaController(activity, mediaController)
            transportControls = null
        }

        override fun onConnectionFailed() {
            Timber.d("onConnectionFailed")
        }
    }

    private val controllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Timber.d("onPlaybackStateChanged $state")
            playbackState = state.state
        }
    }

    init {
        mediaBrowser = MediaBrowserCompat(activity,
                ComponentName(activity, PlayerService::class.java),
                connectionCallbacks, null)
    }

    fun connect() = mediaBrowser.connect()

    fun disconnect() = mediaBrowser.disconnect()

    fun isPlaying(uri: Uri) = playbackState == PlaybackStateCompat.STATE_PLAYING && playingUri == uri

    fun play(uri: Uri) {
        playingUri = uri
        transportControls?.playFromUri(uri, null)
    }

    fun pause() {
        transportControls?.pause()
    }

    fun stop() {
        transportControls?.stop()
    }
}