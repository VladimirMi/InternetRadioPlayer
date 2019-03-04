package io.github.vladimirmi.internetradioplayer.data.repository

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.EqualizerDatabase
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.EqualizerPresetEntity
import io.github.vladimirmi.internetradioplayer.data.utils.AudioEffects
import io.github.vladimirmi.internetradioplayer.data.utils.Preferences
import io.github.vladimirmi.internetradioplayer.domain.model.EqualizerConfig
import io.github.vladimirmi.internetradioplayer.domain.model.EqualizerPreset
import io.github.vladimirmi.internetradioplayer.domain.model.PresetBinder
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 11.01.2019.
 */

class EqualizerRepository
@Inject constructor(db: EqualizerDatabase,
                    private val stationsDb: StationsDatabase,
                    private val preferences: Preferences,
                    private val context: Context) {

    private val dao = db.equalizerDao()
    private val audioEffects = AudioEffects

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var sessionId = 0

    val equalizerConfig: EqualizerConfig
    var presets: List<EqualizerPreset> = emptyList()

    val currentPreset = BehaviorRelay.create<EqualizerPreset>()
    lateinit var binder: PresetBinder

    init {
        if (audioEffects.isEqualizerSupported()) {
            val tempEqualizer = Equalizer(0, 1)
            equalizerConfig = EqualizerConfig.create(tempEqualizer)
            unbindEqualizer(1)
            tempEqualizer.release()
        } else {
            equalizerConfig = EqualizerConfig.empty()
        }
    }

    fun createEqualizer(sessionId: Int) {
        this.sessionId = sessionId
        if (audioEffects.isEqualizerSupported()) {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
        }
        if (audioEffects.isBassBoostSupported()) {
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
        }
        if (audioEffects.isVirtualizerSupported()) {
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true }
        }
        equalizer?.ifHasControl {
            currentPreset.value?.applyTo(equalizer, bassBoost, virtualizer)
        }
    }

    fun releaseEqualizer(sessionId: Int) {
        unbindEqualizer(sessionId)
        equalizer?.release()
        equalizer = null
        bassBoost?.release()
        bassBoost = null
        virtualizer?.release()
        virtualizer = null
    }

    fun setBandLevel(band: Int, level: Int) {
        val preset = currentPreset.value?.let {
            val bands = it.bandLevels.toMutableList()
            bands[band] = level
            it.copy(bandLevels = bands)
        }
        updatePresetsWith(preset)
        equalizer?.ifHasControl {
            it.setBandLevel(band.toShort(), level.toShort())
        }
    }

    fun setBassBoostStrength(strength: Int) {
        updatePresetsWith(currentPreset.value?.copy(bassBoostStrength = strength))
        bassBoost?.ifHasControl {
            it.setStrength(strength.toShort())
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        updatePresetsWith(currentPreset.value?.copy(virtualizerStrength = strength))
        virtualizer?.ifHasControl {
            it.setStrength(strength.toShort())
        }
    }

    fun saveCurrentPreset(): Completable {
        return Completable.fromCallable {
            currentPreset.value?.let { dao.insert(it.toEntity()) }
        }.subscribeOn(Schedulers.io())
    }

    fun createBinder(stationId: String): Single<PresetBinder> {
        return bindPreset()
                .andThen(PresetBinder.create(stationsDb.stationDao(), stationId, preferences.globalPreset))
                .doOnSuccess { binder = it }
                .subscribeOn(Schedulers.io())
    }

    fun selectPreset(index: Int) {
        val preset = if (index >= 0) presets[index] else getGlobalPreset()
        currentPreset.accept(preset)
        if (::binder.isInitialized) binder.presetName = preset.name

        equalizer?.ifHasControl {
            preset.applyTo(equalizer, bassBoost, virtualizer)
        }
    }

    fun getSavedPresets(): Single<List<EqualizerPresetEntity>> {
        return dao.getPresets()
                .subscribeOn(Schedulers.io())
    }

    fun resetCurrentPreset(): Completable {
        val defaultPreset = equalizerConfig?.defaultPresets
                ?.find { it.name == currentPreset.value?.name }
        updatePresetsWith(defaultPreset)
        equalizer?.ifHasControl {
            defaultPreset?.applyTo(equalizer, bassBoost, virtualizer)
        }
        return saveCurrentPreset()
    }

    fun bindPreset(): Completable {
        if (!::binder.isInitialized) return Completable.complete()
        return binder.bind(stationsDb, preferences)
                .subscribeOn(Schedulers.io())
    }

    private fun getGlobalPreset(): EqualizerPreset {
        return presets.find { it.name == preferences.globalPreset } ?: presets.first()
    }

    private fun updatePresetsWith(preset: EqualizerPreset?) {
        preset?.let {
            presets = presets.map { p -> if (p.name == preset.name) preset else p }
            currentPreset.accept(it)
        }
    }

    private fun unbindEqualizer(sessionId: Int) {
        val intent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
        context.sendBroadcast(intent)
    }


    private inline fun <T : AudioEffect> T.ifHasControl(block: (T) -> Unit) {
        if (hasControl()) {
            block(this)
        } else if (sessionId != 0) {
            releaseEqualizer(sessionId)
            createEqualizer(sessionId)
        }
    }
}
