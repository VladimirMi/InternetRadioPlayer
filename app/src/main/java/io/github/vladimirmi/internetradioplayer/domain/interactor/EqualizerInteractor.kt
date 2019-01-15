package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.repository.EqualizerRepository
import io.github.vladimirmi.internetradioplayer.data.repository.PlayerRepository
import io.github.vladimirmi.internetradioplayer.data.service.PlayerService
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.01.2019.
 */

class EqualizerInteractor
@Inject constructor(playerRepository: PlayerRepository,
                    private val equalizerRepository: EqualizerRepository) {

    val bands: List<String>
        get() = equalizerRepository.bands
    val levelRange: Pair<Int, Int>
        get() = equalizerRepository.levelRange
    val bandLevels: List<Int>
        get() = equalizerRepository.equalizerSettings.bandLevels.map { it.toInt() }
    var bassBoost: Int
        get() = equalizerRepository.bassSettings.strength.toInt()
        set(value) = equalizerRepository.setBassBoost(value)
    var virtualizer: Int
        get() = equalizerRepository.virtualizerSettings.strength.toInt()
        set(value) = equalizerRepository.setVirtualizer(value)

    val equalizerInit: Completable = playerRepository.sessionEvent
            .filter { it.first == PlayerService.EVENT_SESSION_ID }
            .map {
                val id = it.second.getInt(PlayerService.EVENT_SESSION_ID, 0)
                if (id != 0) equalizerRepository.initEqualizer(id)
                else equalizerRepository.releaseEqualizer()
            }.ignoreElements()

    fun setBandLevel(band: Int, level: Int) {
        equalizerRepository.setBandLevel(band, level)
    }

    fun getPresets(): Single<List<String>> {
        return Single.just(equalizerRepository.defaultPresets.keys.toList())
    }

    fun getCurrentPreset(): Single<Int> {
        return Single.just(0)
    }

    fun selectPreset(preset: String) {
        equalizerRepository.usePreset(preset)
    }
}