package io.github.vladimirmi.internetradioplayer.domain.interactor

import android.net.Uri
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.StationRepository
import io.github.vladimirmi.internetradioplayer.data.utils.ShortcutHelper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class StationInteractor
@Inject constructor(private val stationRepository: StationRepository,
                    private val favoritesRepository: FavoritesRepository,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val shortcutHelper: ShortcutHelper) {

    val stationObs get() = stationRepository.stationObs
    var station
        get() = stationRepository.station
        set(value) {
            stationRepository.station = value
        }

    fun addCurrentShortcut(startPlay: Boolean): Boolean {
        return shortcutHelper.pinShortcut(station, startPlay)
    }

    fun createStation(uri: Uri): Single<Station> {
        return stationRepository.createStation(uri)
                .doOnSuccess { newStation ->
                    val favoriteStation = favoritesRepository.stations.findStation { it.uri == newStation.uri }
                    station = favoriteStation ?: newStation
                }
    }

    fun addToFavorite(): Completable {
        val newStation = station.copy(order = favoritesRepository.stations.size)
        return favoritesRepository.addStation(newStation)
                .andThen(favoriteListInteractor.initFavoriteList())
                .andThen(setStation(newStation))
    }

    fun removeFromFavorite(): Completable {
        return favoritesRepository.addStation(station)
                .andThen(favoriteListInteractor.initFavoriteList())
                .andThen(setStation(station))
    }

    fun changeGroup(groupName: String): Completable {
        return Single.fromCallable { favoritesRepository.groups.find { it.name == groupName } }
                .flatMapCompletable {
                    if (it.id == station.groupId) Completable.complete()
                    else {
                        val newStation = station.copy(groupId = it.id, order = it.stations.size)
                        favoritesRepository.updateStations(listOf(newStation))
                                .andThen(setStation(newStation))
                                .andThen(favoriteListInteractor.initFavoriteList())
                    }
                }
    }

    fun editStationTitle(title: String): Completable {
        if (title == station.name) return Completable.complete()
        val newStation = station.copy(name = title)
        return favoritesRepository.updateStations(listOf(newStation))
                .andThen(setStation(newStation))
                .andThen(if (favoriteListInteractor.isFavorite(newStation)) {
                    favoriteListInteractor.initFavoriteList()
                } else {
                    Completable.complete()
                })
    }

    private fun setStation(station: Station): Completable {
        return { this.station = station }.toCompletable()
    }
}

