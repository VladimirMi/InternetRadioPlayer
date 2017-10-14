package io.github.vladimirmi.radius.service

import android.app.Activity
import android.content.ComponentName
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import timber.log.Timber

/**
 * Created by Vladimir Mikhalev 13.10.2017.
 */


class PlayerController() {

    private lateinit var activity: Activity
    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    private var playbackState = PlaybackStateCompat.STATE_NONE

    fun create(activity: Activity) {
        this.activity = activity
        mediaBrowser = MediaBrowserCompat(activity,
                ComponentName(activity, PlayerService::class.java),
                connectionCallbacks, null)
    }

    fun connect() {
        mediaBrowser.connect()
    }

    fun disconnect() {
        mediaController?.run { unregisterCallback(controllerCallback) }
        mediaBrowser.disconnect()
    }

    fun isPlaying() = playbackState == PlaybackStateCompat.STATE_PLAYING

    fun playMedia() {
        transportControls?.run { play() }
    }

    fun pauseMedia() {
        transportControls?.run { pause() }
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Timber.d("onConnected: session token " + mediaBrowser.sessionToken)

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

    val controllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Timber.d("onPlaybackStateChanged ", state)
            playbackState = state.state
        }
    }
}