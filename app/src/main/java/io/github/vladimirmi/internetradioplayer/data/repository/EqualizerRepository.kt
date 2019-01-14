package io.github.vladimirmi.internetradioplayer.data.repository

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 11.01.2019.
 */

class EqualizerRepository
@Inject constructor() {

    private var defaultEqualizer = Equalizer(-1, 1)
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    var equalizerSettings: Equalizer.Settings
    val bassSettings = BassBoost.Settings()
    val virtualizerSettings = Virtualizer.Settings()
    val bands: List<String>
    val levelRange: Pair<Int, Int>
    val defaultPresets: List<String>

    init {
        with(defaultEqualizer) {
            bands = (0 until numberOfBands).map {
                val freq = getCenterFreq(it.toShort()) / 1000 //Hz
                if (freq > 1000) "${freq / 1000.0} kHz" else "$freq Hz"
            }
            levelRange = bandLevelRange[0].toInt() to bandLevelRange[1].toInt()
            defaultPresets = (0 until numberOfPresets).map { getPresetName(it.toShort()) }
            equalizerSettings = properties
        }
    }

    fun initEqualizer(sessionId: Int) {
        equalizer = Equalizer(0, sessionId)
        equalizer?.enabled = true
        equalizer?.properties = equalizerSettings

        bassBoost = BassBoost(0, sessionId)
        bassBoost?.enabled = true
        bassBoost?.properties = bassSettings

        virtualizer = Virtualizer(0, sessionId)
        virtualizer?.enabled = true
        virtualizer?.properties = virtualizerSettings
    }

    fun releaseEqualizer() {
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

        equalizer?.usePreset(1)
    }

    fun setBassBoost(strength: Int) {
        bassSettings.strength = strength.toShort()
        bassBoost?.setStrength(strength.toShort())
    }

    fun setVirtualizer(strength: Int) {
        virtualizerSettings.strength = strength.toShort()
        virtualizer?.setStrength(strength.toShort())
    }

    fun usePreset(preset: Int) {
        defaultEqualizer.usePreset(preset.toShort())
        equalizerSettings = defaultEqualizer.properties
        equalizer?.usePreset(preset.toShort())
    }
}