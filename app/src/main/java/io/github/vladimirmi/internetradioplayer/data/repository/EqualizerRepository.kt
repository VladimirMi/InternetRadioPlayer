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
import io.github.vladimirmi.internetradioplayer.data.preference.Preferences
import io.github.vladimirmi.internetradioplayer.data.utils.AudioEffects
import io.github.vladimirmi.internetradioplayer.domain.model.EqualizerConfig
import io.github.vladimirmi.internetradioplayer.domain.model.EqualizerPreset
import io.github.vladimirmi.internetradioplayer.domain.model.PresetBinder
import io.github.vladimirmi.internetradioplayer.extensions.runOnUiThreadDelayed
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
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

    val currentPresetObs = BehaviorRelay.create<EqualizerPreset>()
    var currentPreset: EqualizerPreset
        get() = currentPresetObs.value ?: EqualizerPreset.empty()
        private set(value) {
            equalizer?.ifHasControl { value.applyTo(equalizer, bassBoost, virtualizer) }
            currentPresetObs.accept(value)
        }
    private var equalizerEnabled = preferences.equalizerEnabled
        set(value) {
            preferences.equalizerEnabled = value
            field = value
        }
    val equalizerEnabledObs = BehaviorRelay.createDefault(equalizerEnabled)

    lateinit var binder: PresetBinder

    init {
        equalizerConfig = if (audioEffects.isEqualizerSupported()) {
            try {
                val tempEqualizer = Equalizer(0, 1)
                val config = EqualizerConfig.create(tempEqualizer)
                tempEqualizer.release()
                unbindEqualizer(1)
                config
            } catch (e: Exception) {
                Timber.e(e)
                EqualizerConfig.empty()
            }
        } else {
            EqualizerConfig.empty()
        }
    }

    fun createEqualizer(sessionId: Int, checkControl: Boolean = true) {
        this.sessionId = sessionId
        if (!equalizerEnabled || sessionId == 0) return
        try {
            if (audioEffects.isEqualizerSupported()) equalizer = Equalizer(0, sessionId)
            if (audioEffects.isBassBoostSupported()) bassBoost = BassBoost(0, sessionId)
            if (audioEffects.isVirtualizerSupported()) virtualizer = Virtualizer(0, sessionId)

            // on some devices with built-in equalizer the audio effect loses control right after creation
            runOnUiThreadDelayed(300) {
                equalizer?.enabled = true
                bassBoost?.enabled = true
                virtualizer?.enabled = true
                if (checkControl) equalizer?.ifHasControl {
                    currentPreset.applyTo(equalizer, bassBoost, virtualizer)
                } else {
                    currentPreset.applyTo(equalizer, bassBoost, virtualizer)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun releaseEqualizer(sessionId: Int) {
        equalizer?.release()
        equalizer = null
        bassBoost?.release()
        bassBoost = null
        virtualizer?.release()
        virtualizer = null
        unbindEqualizer(sessionId)
    }

    fun enableEqualizer(enabled: Boolean) {
        equalizerEnabled = enabled
        if (enabled && equalizer == null) {
            createEqualizer(sessionId)
        } else {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        }
        equalizerEnabledObs.accept(enabled)
    }

    fun setBandLevel(band: Int, level: Int) {
        val preset = currentPreset.let {
            val bands = it.bandLevels.toMutableList()
            bands[band] = level
            it.copy(bandLevels = bands)
        }
        applyPreset(preset)
    }

    fun setBassBoostStrength(strength: Int) {
        applyPreset(currentPreset.copy(bassBoostStrength = strength))
    }

    fun setVirtualizerStrength(strength: Int) {
        applyPreset(currentPreset.copy(virtualizerStrength = strength))
    }

    fun saveCurrentPreset(): Completable {
        return Completable.fromCallable {
            currentPreset.let { dao.insert(it.toEntity()) }
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
        if (::binder.isInitialized) binder.presetName = preset.name
        currentPreset = preset
    }

    fun getSavedPresets(): Single<List<EqualizerPresetEntity>> {
        return dao.getPresets()
                .subscribeOn(Schedulers.io())
    }

    fun resetCurrentPreset(): Completable {
        val defaultPreset = equalizerConfig.defaultPresets.find { it.name == currentPreset.name }
                ?: return Completable.complete()
        applyPreset(defaultPreset)
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

    private fun applyPreset(preset: EqualizerPreset) {
        presets = presets.map { p -> if (p.name == preset.name) preset else p }
        currentPreset = preset
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
            createEqualizer(sessionId, checkControl = false)
        }
    }
}
