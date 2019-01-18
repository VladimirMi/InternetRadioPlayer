package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.repository.EqualizerRepository
import io.github.vladimirmi.internetradioplayer.data.repository.PlayerRepository
import io.github.vladimirmi.internetradioplayer.data.repository.StationRepository
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.github.vladimirmi.internetradioplayer.domain.model.EqualizerPreset
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.01.2019.
 */

class EqualizerInteractor
@Inject constructor(private val playerRepository: PlayerRepository,
                    private val equalizerRepository: EqualizerRepository,
                    private val stationRepository: StationRepository) {

    val currentPresetObs: Observable<EqualizerPreset> get() = equalizerRepository.currentPresetObs
    val binderIconResId: Int get() = equalizerRepository.binder.iconResId
    val equalizerConfig get() = equalizerRepository.equalizerConfig

    fun initEqualizer(): Completable {
        return playerRepository.sessionEvent
                .filter { it.first == PlayerService.EVENT_SESSION_ID }
                .map { it.second.getInt(PlayerService.EVENT_SESSION_ID, 0) }
                .map {
                    if (it != 0) equalizerRepository.createEqualizer(it)
                    else equalizerRepository.releaseEqualizer()
                }.ignoreElements()
    }

    fun initPresets(): Completable {
        return equalizerRepository.getSavedPresets().map { entities ->
            val presets = entities.map { EqualizerPreset.create(it) }
            val defaultPresets = equalizerRepository.equalizerConfig.defaultPresets
            val result = ArrayList<EqualizerPreset>(presets.size + defaultPresets.size)
            result.addAll(defaultPresets)
            presets.forEach { preset ->
                val index = result.indexOfFirst { it.name == preset.name }
                if (index != -1) result[index] = preset
                else result += preset
            }
            equalizerRepository.presets = result
        }.ignoreElement()
                .andThen(initCurrentPreset())
    }

    private fun initCurrentPreset(): Completable {
        return stationRepository.stationObs
                .flatMapSingle { equalizerRepository.getPresetBinder(it.id) }
                .map {
                    val (presetName, binder) = it

                    val preset = equalizerRepository.presets.find { preset -> preset.name == presetName }
                            ?: equalizerRepository.getGlobalPreset()

                    equalizerRepository.setPreset(preset, binder)

                }.ignoreElements()
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

    fun switchBind(): Completable {
        return equalizerRepository.switchBind()
    }
}