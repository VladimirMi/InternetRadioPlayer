package io.github.vladimirmi.internetradioplayer.data.service

import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioManager.*
import android.net.Uri
import android.net.wifi.WifiManager
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import io.github.vladimirmi.internetradioplayer.BuildConfig
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.source.IcyDataSource
import io.github.vladimirmi.internetradioplayer.di.Scopes

private const val VOLUME_DUCK = 0.2f
private const val VOLUME_NORMAL = 1.0f
const val STOP_DELAY = 60000L // default stop delay 1 min
private const val STOP_DELAY_HEADSET = 3 * 60000L // stop delay on headset unplug

class Playback(private val service: PlayerService,
               private val playerCallback: PlayerCallback)
    : AudioManager.OnAudioFocusChangeListener {

    private var playAgainOnFocus = false
    private var playAgainOnHeadset = false
    private var player: SimpleExoPlayer? = null
    private val loadControl = Scopes.app.getInstance(LoadControl::class.java)
    private val audioRenderers = AudioRenderersFactory(service)

    private val wifiLock = (service.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, BuildConfig.APPLICATION_ID)
    private val audioManager = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun play(uri: Uri) {
        if (player == null) createPlayer()
        preparePlayer(uri)
        if (holdResources()) resume()
    }

    fun resume() {
        player?.playWhenReady = true
    }

    fun pause() {
        player?.playWhenReady = false
    }

    fun stop() {
        playAgainOnFocus = false
        playAgainOnHeadset = false
        releaseResources()
        player?.stop()
    }

    fun releasePlayer() {
        player?.removeListener(playerCallback)
        player?.release()
        player = null
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AUDIOFOCUS_GAIN -> {
                player?.volume = VOLUME_NORMAL
                if (playAgainOnFocus) resume()
            }
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player?.volume = VOLUME_DUCK
            AUDIOFOCUS_LOSS, AUDIOFOCUS_LOSS_TRANSIENT -> {
                playAgainOnFocus = player?.playWhenReady ?: false
                pause()
            }
            else -> pause()
        }
    }

    private fun createPlayer() {
        val trackSelector = DefaultTrackSelector()
        player = ExoPlayerFactory.newSimpleInstance(service, audioRenderers, trackSelector, loadControl)
        player?.addListener(playerCallback)
    }

    private fun preparePlayer(uri: Uri) {
        val userAgent = Util.getUserAgent(service, service.getString(R.string.app_name))
        val mediaSource = ExtractorMediaSource.Factory { IcyDataSource(userAgent, playerCallback) }
                .createMediaSource(uri)

        player?.prepare(mediaSource)
    }

    private fun holdResources(): Boolean {
        registerAudioNoisyReceiver()
        if (!wifiLock.isHeld) wifiLock.acquire()
        @Suppress("DEPRECATION")
        return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun releaseResources() {
        unregisterAudioNoisyReceiver()
        if (wifiLock.isHeld) wifiLock.release()
        @Suppress("DEPRECATION")
        audioManager.abandonAudioFocus(this)
    }

    private val audioNoisyReceiver = object : BroadcastReceiver() {
        val filter = IntentFilter(ACTION_AUDIO_BECOMING_NOISY).apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        }

        @Suppress("DEPRECATION")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when {
                action == AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    playAgainOnHeadset = player?.playWhenReady ?: false
                    service.onPauseCommand(stopDelay = STOP_DELAY_HEADSET)

                }
                playAgainOnHeadset && action == Intent.ACTION_HEADSET_PLUG
                        && intent.getIntExtra("state", 0) == 1
                        ||
                        playAgainOnHeadset && action == BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED
                        && intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0) == BluetoothHeadset.STATE_CONNECTED
                -> service.onPlayCommand()
            }
        }
    }

    @Volatile private var audioNoisyReceiverRegistered = false

    private fun registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            service.registerReceiver(audioNoisyReceiver, audioNoisyReceiver.filter)
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
