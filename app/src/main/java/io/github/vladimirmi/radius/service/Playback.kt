package io.github.vladimirmi.radius.service

import android.bluetooth.BluetoothDevice
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


class Playback(private val service: PlayerService,
               private val playerCallback: EmptyPlayerCallback)
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
    private var player: SimpleExoPlayer? = null

    private val wifiLock = (service.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, BuildConfig.APPLICATION_ID)
    private val audioManager = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun play(uri: Uri) {
        stop(releaseResources = false)
        Timber.d("play")
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

    override fun onAudioFocusChange(focusChange: Int) {
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
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = NO_FOCUSED_NO_DUCK
            }
        }
    }

    private fun createPlayer() {
        Timber.d("createPlayer")
        val renderersFactory = DefaultRenderersFactory(service)
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
        registerAudioNoisyReceiver()
        if (!wifiLock.isHeld) wifiLock.acquire()
    }

    private fun releaseResources() {
        Timber.d("releaseResources")
        giveUpAudioFocus()
        unregisterAudioNoisyReceiver()
        if (wifiLock.isHeld) wifiLock.release()
    }

    private val audioNoisyReceiver = object : BroadcastReceiver() {
        val filter = IntentFilter(ACTION_AUDIO_BECOMING_NOISY).apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        }

        @Suppress("DEPRECATION")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()

            } else if (action == Intent.ACTION_HEADSET_PLUG && intent.getIntExtra("state", 0) == 1) {
                resume()

            } else if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
                var count = 0
                while (!audioManager.isBluetoothA2dpOn && count < 10) {
                    Thread.sleep(1000)
                    count++
                }
                if (audioManager.isBluetoothA2dpOn) {
                    resume()
                }
            }
        }
    }

    private fun registerAudioNoisyReceiver() {
        service.registerReceiver(audioNoisyReceiver, audioNoisyReceiver.filter)
    }

    private fun unregisterAudioNoisyReceiver() {
        service.unregisterReceiver(audioNoisyReceiver)
    }
}
