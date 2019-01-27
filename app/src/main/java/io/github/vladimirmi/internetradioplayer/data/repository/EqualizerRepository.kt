package io.github.vladimirmi.internetradioplayer.data.repository

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.EqualizerDatabase
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.EqualizerPresetEntity
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
                    private val preferences: Preferences) {

    private val dao = db.equalizerDao()
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    val equalizerConfig: EqualizerConfig
    var presets: List<EqualizerPreset> = emptyList()

    val currentPreset = BehaviorRelay.create<EqualizerPreset>()
    lateinit var binder: PresetBinder

    init {
        val tempEqualizer = Equalizer(-1, 1)
        equalizerConfig = EqualizerConfig.create(tempEqualizer)
        tempEqualizer.release()
    }

    fun createEqualizer(sessionId: Int) {
        equalizer = Equalizer(0, sessionId).apply { enabled = true }
        bassBoost = BassBoost(0, sessionId).apply { enabled = true }
        virtualizer = Virtualizer(0, sessionId).apply { enabled = true }

        currentPreset.value?.applyTo(equalizer, bassBoost, virtualizer)
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
        val preset = currentPreset.value?.let {
            val bands = it.bandLevels.toMutableList()
            bands[band] = level
            it.copy(bandLevels = bands)
        }
        updatePresetsWith(preset)
        equalizer?.setBandLevel(band.toShort(), level.toShort())
    }

    fun setBassBoostStrength(strength: Int) {
        updatePresetsWith(currentPreset.value?.copy(bassBoostStrength = strength))
        bassBoost?.setStrength(strength.toShort())
    }

    fun setVirtualizerStrength(strength: Int) {
        updatePresetsWith(currentPreset.value?.copy(virtualizerStrength = strength))
        virtualizer?.setStrength(strength.toShort())
    }

    fun saveCurrentPreset(): Completable {
        return Completable.fromCallable {
            currentPreset.value?.let { dao.insert(it.toEntity()) }
        }.subscribeOn(Schedulers.io())
    }

    fun createBinder(stationId: String): Single<PresetBinder> {
        return bindPreset().andThen(PresetBinder.create(stationsDb.stationDao(), stationId, preferences.globalPreset))
                .doOnSuccess { binder = it }
                .subscribeOn(Schedulers.io())
    }

    fun selectPreset(index: Int) {
        val preset = if (index >= 0) presets[index] else getGlobalPreset()
        preset.applyTo(equalizer, bassBoost, virtualizer)
        currentPreset.accept(preset)
        if (::binder.isInitialized) binder.presetName = preset.name
    }

    fun getSavedPresets(): Single<List<EqualizerPresetEntity>> {
        return dao.getPresets()
                .subscribeOn(Schedulers.io())
    }

    fun resetCurrentPreset(): Completable {
        val defaultPreset = equalizerConfig.defaultPresets.find { it.name == currentPreset.value?.name }
        defaultPreset?.applyTo(equalizer, bassBoost, virtualizer)
        updatePresetsWith(defaultPreset)
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
}
