package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.repository.EqualizerRepository
import io.github.vladimirmi.internetradioplayer.data.repository.MediaRepository
import io.github.vladimirmi.internetradioplayer.data.repository.PlayerRepository
import io.github.vladimirmi.internetradioplayer.data.service.EVENT_SESSION_END
import io.github.vladimirmi.internetradioplayer.data.service.EVENT_SESSION_START
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.github.vladimirmi.internetradioplayer.domain.model.EqualizerPreset
import io.github.vladimirmi.internetradioplayer.domain.model.PresetBinderView
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.01.2019.
 */

class EqualizerInteractor
@Inject constructor(private val playerRepository: PlayerRepository,
                    private val equalizerRepository: EqualizerRepository,
                    private val mediaRepository: MediaRepository) {

    val currentPresetObs: Observable<EqualizerPreset> get() = equalizerRepository.currentPreset
    val presetBinder: PresetBinderView get() = equalizerRepository.binder
    val equalizerConfig get() = equalizerRepository.equalizerConfig

    fun initEqualizer(): Completable {
        return playerRepository.sessionEvent
                .map {
                    val sessionId = it.second.getInt(PlayerService.EXTRA_SESSION_ID)
                    if (sessionId != 0) {
                        if (it.first == EVENT_SESSION_START) {
                            equalizerRepository.createEqualizer(sessionId)
                        } else if (it.first == EVENT_SESSION_END) {
                            equalizerRepository.releaseEqualizer(sessionId)
                        }
                    }
                }.ignoreElements()
    }

    fun initPresets(): Completable {
        return equalizerRepository.getSavedPresets().map { entities ->
            val savedPresets = entities.map { EqualizerPreset.create(it) }
            val presets = equalizerRepository.equalizerConfig.defaultPresets.toMutableList()
            presets.forEachIndexed { index, preset ->
                savedPresets.find { it.name == preset.name }?.let { presets[index] = it }
            }
            equalizerRepository.presets = presets
        }
                .ignoreElement()
                .andThen(initCurrentPreset())
    }

    private fun initCurrentPreset(): Completable {
        return mediaRepository.currentMediaObs
                .flatMapSingle { equalizerRepository.createBinder(it.id) }
                .map { equalizerRepository.presets.indexOfFirst { preset -> preset.name == it.presetName } }
                .doOnNext { equalizerRepository.selectPreset(it) }
                .ignoreElements()
    }

    fun setBandLevel(band: Int, level: Int) {
        equalizerRepository.setBandLevel(band, level)
    }

    fun setBassBoostStrength(strength: Int) {
        equalizerRepository.setBassBoostStrength(strength)
    }

    fun setVirtualizerStrength(strength: Int) {
        equalizerRepository.setVirtualizerStrength(strength)
    }

    fun getPresetNames(): List<String> {
        return equalizerRepository.presets.map { it.name }
    }

    fun selectPreset(index: Int) {
        equalizerRepository.selectPreset(index)
    }

    fun saveCurrentPreset(): Completable {
        return equalizerRepository.saveCurrentPreset()
    }

    fun switchBind() {
        equalizerRepository.binder = equalizerRepository.binder.nextBinder()
    }

    fun bindPreset(): Completable {
        return equalizerRepository.bindPreset()
    }

    fun resetCurrentPreset(): Completable {
        return equalizerRepository.resetCurrentPreset()
    }

    fun isCurrentPresetCanReset(): Boolean {
        val preset = equalizerRepository.currentPreset.value
        val defaultPreset = equalizerConfig.defaultPresets.find { it.name == preset?.name }
        return preset != defaultPreset
    }
}