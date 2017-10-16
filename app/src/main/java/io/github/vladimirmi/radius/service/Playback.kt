package io.github.vladimirmi.radius.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioManager.*
import android.net.Uri
import android.net.wifi.WifiManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import io.github.vladimirmi.radius.BuildConfig
import io.github.vladimirmi.radius.service.Playback.AudioFocus.*
import timber.log.Timber


class Playback(private val service: PlayerService, private val playerCallback: EmptyPlayerCallback)
    : AudioManager.OnAudioFocusChangeListener {

    companion object {
        private val VOLUME_DUCK = 0.2f
        private val VOLUME_NORMAL = 1.0f
    }

    enum class AudioFocus {
        NO_FOCUSED_NO_DUCK,
        NO_FOCUSED_CAN_DUCK,
        FOCUSED
    }

    private var audioFocus = NO_FOCUSED_NO_DUCK
    private var playAgainOnFocus = false
    private var audioNoisyReceiverRegistered = false
    private var player: SimpleExoPlayer? = null

    private val wifiLock = (service.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, BuildConfig.APPLICATION_ID)
    private val audioManager = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val isPlaying: Boolean
        get() = playAgainOnFocus || player?.playWhenReady ?: false

    fun play(uri: Uri) {
        Timber.d("play")
        stop(releaseResources = false)
        holdResources()
        if (player == null) createPlayer()
        preparePlayer(uri)
        resume()
    }

    fun resume() {
        Timber.d("resume")
        playAgainOnFocus = true
        player?.playWhenReady = true
    }

    fun pause() {
        Timber.d("pause")
        playAgainOnFocus = false
        player?.playWhenReady = false
    }

    fun stop(releaseResources: Boolean = true) {
        Timber.d("stop")
        if (releaseResources) releaseResources()
        playAgainOnFocus = false
        player?.stop()
    }

    fun releasePlayer() {
        Timber.d("releasePlayer")
        player?.removeListener(playerCallback)
        player?.release()
        player = null
    }

    /**
     * Called by AudioManager on audio focus changes.
     * Implementation of [AudioManager.OnAudioFocusChangeListener]
     */
    override fun onAudioFocusChange(focusChange: Int) {
        Timber.d("onAudioFocusChange: focusChange=$focusChange")

        audioFocus = when (focusChange) {
            AUDIOFOCUS_GAIN -> FOCUSED
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> NO_FOCUSED_CAN_DUCK
            AUDIOFOCUS_LOSS, AUDIOFOCUS_LOSS_TRANSIENT -> {
                playAgainOnFocus = player!!.playWhenReady
                NO_FOCUSED_NO_DUCK
            }
            else -> NO_FOCUSED_NO_DUCK
        }
        configPlayerState()
    }

    private fun configPlayerState() {
        Timber.d("configPlayerState. audioFocus=", audioFocus)
        when (audioFocus) {
            FOCUSED -> {
                player?.volume = VOLUME_NORMAL
                if (playAgainOnFocus) resume()
            }
            NO_FOCUSED_CAN_DUCK -> player?.volume = VOLUME_DUCK
            NO_FOCUSED_NO_DUCK -> pause()
        }
    }

    @Suppress("DEPRECATION")
    private fun tryToGetAudioFocus() {
        if (audioFocus != FOCUSED) {
            Timber.d("tryToGetAudioFocus")
            val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AUDIOFOCUS_GAIN)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = FOCUSED
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun giveUpAudioFocus() {
        if (audioFocus == FOCUSED) {
            Timber.d("giveUpAudioFocus")
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = NO_FOCUSED_NO_DUCK
            }
        }
    }

    private fun createPlayer() {
        Timber.d("createPlayer")
        val renderersFactory = DefaultRenderersFactory(service.applicationContext)
        val trackSelector = DefaultTrackSelector()
        val loadControl = DefaultLoadControl(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE * 2))
        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl)
        player?.addListener(playerCallback)
    }

    private fun preparePlayer(uri: Uri) {
        val dataSourceFactory = DefaultHttpDataSourceFactory(BuildConfig.APPLICATION_ID, DefaultBandwidthMeter())
        val mediaSource = ExtractorMediaSource(uri, dataSourceFactory, DefaultExtractorsFactory(),
                32, null, null, null, 1024 * 1024)
        player?.prepare(mediaSource)
    }

    private fun holdResources() {
        Timber.d("holdResources")
        tryToGetAudioFocus()
        //todo implement
//        service.startForeground()
        registerAudioNoisyReceiver()
        if (!wifiLock.isHeld) wifiLock.acquire()
    }

    private fun releaseResources() {
        Timber.d("releaseResources")
        giveUpAudioFocus()
//        service.stopForeground(true)
        unregisterAudioNoisyReceiver()
        if (wifiLock.isHeld) wifiLock.release()
    }

    private val audioNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                Timber.d("Headphones disconnected.")
                if (isPlaying) {
                    val i = Intent(context, PlayerService::class.java).apply {
                        action = PlayerService.ACTION_STOP
                    }
                    service.startService(i)
                }
            }
        }
    }

    private fun registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            service.registerReceiver(audioNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            audioNoisyReceiverRegistered = true
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (audioNoisyReceiverRegistered) {
            service.unregisterReceiver(audioNoisyReceiver)
            audioNoisyReceiverRegistered = false
        }
    }
}
