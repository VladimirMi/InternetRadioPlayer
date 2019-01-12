package io.github.vladimirmi.internetradioplayer.data.repository

import android.media.audiofx.Equalizer
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 11.01.2019.
 */

class EqualizerRepository
@Inject constructor(playerRepository: PlayerRepository) {

    private val defaultEqualizer = Equalizer(0, 666)
    private var equalizer: Equalizer? = null
    val bands: List<String>
    val levelRange: Pair<Int, Int>
    val eqSettings: Equalizer.Settings

    init {
        with(defaultEqualizer) {
            bands = (0 until numberOfBands).map {
                val freq = getCenterFreq(it.toShort()) / 1000 //Hz
                if (freq > 1000) "${freq / 1000.0} kHz" else "$freq Hz"
            }
            levelRange = bandLevelRange[0].toInt() to bandLevelRange[1].toInt()

            eqSettings = properties
        }
    }

    val equalizerObs = playerRepository.sessionEvent
            .filter { it.first == PlayerService.EVENT_SESSION_ID }
            .map {
                val id = it.second.getInt(PlayerService.EVENT_SESSION_ID, 0)
                equalizer = Equalizer(1, id)
                equalizer?.enabled = true
                equalizer?.properties = eqSettings
            }


    fun setBandLevel(band: Int, level: Int) {
        eqSettings.curPreset = -1
        eqSettings.bandLevels[band] = level.toShort()
        equalizer?.setBandLevel(band.toShort(), level.toShort())
    }
}