package io.github.vladimirmi.internetradioplayer.data.repository

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.10.2017.
 */

class PlayerRepository
@Inject constructor(context: Context) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var controller: MediaControllerCompat? = null

    val playbackState: BehaviorRelay<PlaybackStateCompat> = BehaviorRelay.create()
    val metadata: BehaviorRelay<MediaMetadataCompat> = BehaviorRelay.create()
    val sessionEvent: BehaviorRelay<Pair<String, Bundle>> = BehaviorRelay.create()
    private val connected = BehaviorRelay.createDefault(false)

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                controller = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                    registerCallback(controllerCallback)
                    controllerCallback.onPlaybackStateChanged(playbackState)
                    controllerCallback.onMetadataChanged(metadata)
                }
                connected.accept(true)
            } catch (e: RemoteException) {
                Timber.e(e)
            }
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
            playbackState.accept(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            this@PlayerRepository.metadata.accept(metadata)
        }

        override fun onSessionEvent(event: String, extras: Bundle) {
            sessionEvent.accept(event to extras)
        }
    }

    init {
        mediaBrowser = MediaBrowserCompat(context,
                ComponentName(context, PlayerService::class.java),
                connectionCallbacks, null)
    }

    fun connect() {
        if (!mediaBrowser.isConnected) mediaBrowser.connect()
    }

    fun disconnect() {
        mediaBrowser.disconnect()
        connected.accept(false)
    }

    @SuppressLint("CheckResult")
    fun play() {
        connected.filter { it }.first(true).subscribeBy { controller?.transportControls?.play() }
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

    fun seekTo(position: Long) {
        controller?.transportControls?.seekTo(position)
    }

    fun sendCommand(command: String) {
        controller?.sendCommand(command, null, null)
    }
}
