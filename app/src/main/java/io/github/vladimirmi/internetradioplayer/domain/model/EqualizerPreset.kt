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
        fun create(entity: EqualizerPresetEntity): EqualizerPreset {
            return create(entity.name,
                    Equalizer.Settings(entity.bands),
                    BassBoost.Settings(entity.bass),
                    Virtualizer.Settings(entity.virtualizer))
        }

        fun create(name: String, eS: Equalizer.Settings,
                   bS: BassBoost.Settings = BassBoost.Settings(),
                   vS: Virtualizer.Settings = Virtualizer.Settings())
                : EqualizerPreset {
            return EqualizerPreset(name,
                    eS.bandLevels.map { it.toInt() },
                    bS.strength.toInt(),
                    vS.strength.toInt())
        }
    }

    fun applyTo(equalizer: Equalizer?, bassBoost: BassBoost?, virtualizer: Virtualizer?) {
        bandLevels.forEachIndexed { band, level ->
            equalizer?.setBandLevel(band.toShort(), level.toShort())
        }
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