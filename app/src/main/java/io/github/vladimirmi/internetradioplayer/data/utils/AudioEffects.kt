package io.github.vladimirmi.internetradioplayer.data.utils

import android.media.audiofx.AudioEffect
import java.util.*

/**
 * Created by Vladimir Mikhalev 04.03.2019.
 */

object AudioEffects {

    /**
     * UUID for equalizer effect
     */
    private val EFFECT_TYPE_EQUALIZER: UUID = UUID.fromString("0bed4300-ddd6-11db-8f34-0002a5d5c51b")
    /**
     * UUID for bass boost effect
     */
    private val EFFECT_TYPE_BASS_BOOST: UUID = UUID.fromString("0634f220-ddd4-11db-a0fc-0002a5d5c51b")
    /**
     * UUID for virtualizer effect
     */
    private val EFFECT_TYPE_VIRTUALIZER: UUID = UUID.fromString("37cc2c00-dddd-11db-8577-0002a5d5c51b")

    private val supportedEffects = AudioEffect.queryEffects()
            .mapTo(HashSet(), AudioEffect.Descriptor::type)


    fun isEqualizerSupported(): Boolean {
        return supportedEffects.contains(EFFECT_TYPE_EQUALIZER)
    }

    fun isBassBoostSupported(): Boolean {
        return supportedEffects.contains(EFFECT_TYPE_BASS_BOOST)
    }

    fun isVirtualizerSupported(): Boolean {
        return supportedEffects.contains(EFFECT_TYPE_VIRTUALIZER)
    }
}