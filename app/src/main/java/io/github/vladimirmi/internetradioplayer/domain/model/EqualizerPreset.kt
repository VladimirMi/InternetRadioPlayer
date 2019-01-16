package io.github.vladimirmi.internetradioplayer.domain.model

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import io.github.vladimirmi.internetradioplayer.data.db.entity.EqualizerPresetEntity

/**
 * Created by Vladimir Mikhalev 16.01.2019.
 */

data class EqualizerPreset(val name: String,
                           val bandLevels: List<Int>,
                           val bassBoostStrength: Int,
                           val virtualizerStrength: Int) {

    companion object {
        fun create(name: String, settings: Equalizer.Settings): EqualizerPreset {
            return EqualizerPreset(name,
                    settings.bandLevels.map { it.toInt() },
                    0, 0)
        }

        fun create(entity: EqualizerPresetEntity): EqualizerPreset {
            return EqualizerPreset(entity.name,
                    Equalizer.Settings(entity.bands).bandLevels.map { it.toInt() },
                    BassBoost.Settings(entity.bass).strength.toInt(),
                    Virtualizer.Settings(entity.virtualizer).strength.toInt())
        }
    }

    fun applyTo(equalizer: Equalizer?, bassBoost: BassBoost?, virtualizer: Virtualizer?) {
        equalizer?.properties = equalizerSettings()
        bassBoost?.setStrength(bassBoostStrength.toShort())
        virtualizer?.setStrength(virtualizerStrength.toShort())
    }

    fun toEntity(): EqualizerPresetEntity {
        return EqualizerPresetEntity(name,
                equalizerSettings().toString(),
                BassBoost.Settings().apply { strength = bassBoostStrength.toShort() }.toString(),
                Virtualizer.Settings().apply { strength = virtualizerStrength.toShort() }.toString()
        )
    }

    private fun equalizerSettings(): Equalizer.Settings {
        val eS = Equalizer.Settings()
        eS.curPreset = -1
        eS.numBands = bandLevels.size.toShort()
        eS.bandLevels = ShortArray(bandLevels.size) { bandLevels[it].toShort() }
        return eS
    }
}