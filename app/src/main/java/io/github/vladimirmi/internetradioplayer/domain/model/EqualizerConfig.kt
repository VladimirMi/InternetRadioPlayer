package io.github.vladimirmi.internetradioplayer.domain.model

import android.media.audiofx.Equalizer

/**
 * Created by Vladimir Mikhalev 16.01.2019.
 */

class EqualizerConfig(val bands: List<String>,
                      val minLevel: Int,
                      val maxLevel: Int,
                      val defaultPresets: List<EqualizerPreset>) {

    companion object {
        fun create(equalizer: Equalizer): EqualizerConfig {
            val bands = (0 until equalizer.numberOfBands).map {
                val freq = equalizer.getCenterFreq(it.toShort()) / 1000 //Hz
                if (freq > 1000) "${freq / 1000.0} kHz" else "$freq Hz"
            }
            val levelRange = equalizer.bandLevelRange

            val presets = (0 until equalizer.numberOfPresets)
                    .map {
                        equalizer.usePreset(it.toShort())
                        EqualizerPreset.create(equalizer.getPresetName(it.toShort()), equalizer.properties)
                    }
            return EqualizerConfig(
                    bands,
                    levelRange[0].toInt(),
                    levelRange[1].toInt(),
                    presets
            )
        }

        fun empty() = EqualizerConfig(emptyList(), 0, 0, emptyList())
    }
}