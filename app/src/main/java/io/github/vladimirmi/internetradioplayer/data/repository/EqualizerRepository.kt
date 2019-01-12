package io.github.vladimirmi.internetradioplayer.data.repository

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 11.01.2019.
 */

class EqualizerRepository
@Inject constructor(playerRepository: PlayerRepository) {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private val equalizerSettings: Equalizer.Settings
    val bands: List<String>
    val levelRange: Pair<Int, Int>

    init {
        with(Equalizer(-1, 1)) {
            bands = (0 until numberOfBands).map {
                val freq = getCenterFreq(it.toShort()) / 1000 //Hz
                if (freq > 1000) "${freq / 1000.0} kHz" else "$freq Hz"
            }
            levelRange = bandLevelRange[0].toInt() to bandLevelRange[1].toInt()

            equalizerSettings = properties
            release()
        }
    }

    val equalizerObs: Completable = playerRepository.sessionEvent
            .filter { it.first == PlayerService.EVENT_SESSION_ID }
            .map {
                val id = it.second.getInt(PlayerService.EVENT_SESSION_ID, 0)
                if (id != 0) initEqualizer(id)
                else releaseEqualizer()
            }.ignoreElements()

    private fun initEqualizer(sessionId: Int) {
        equalizer = Equalizer(0, sessionId)
        equalizer?.enabled = true
        equalizer?.properties = equalizerSettings

        bassBoost = BassBoost(0, sessionId)
        bassBoost?.enabled = true
        virtualizer = Virtualizer(0, sessionId)
        virtualizer?.enabled = true
    }

    private fun releaseEqualizer() {
        equalizer?.release()
        equalizer = null
        bassBoost?.release()
        bassBoost = null
        virtualizer?.release()
        virtualizer = null
    }

    fun setBandLevel(band: Int, level: Int) {
        equalizerSettings.curPreset = -1
        equalizerSettings.bandLevels[band] = level.toShort()
        equalizer?.setBandLevel(band.toShort(), level.toShort())
    }

    fun setBassBoost(strength: Int) {
        bassBoost?.setStrength(strength.toShort())
    }

    fun setVirtualizer(strength: Int) {
        virtualizer?.setStrength(strength.toShort())
    }
}