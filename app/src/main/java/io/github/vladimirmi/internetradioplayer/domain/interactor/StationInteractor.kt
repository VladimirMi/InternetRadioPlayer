package io.github.vladimirmi.internetradioplayer.domain.interactor

import android.net.Uri
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.GroupListRepository
import io.github.vladimirmi.internetradioplayer.data.repository.StationRepository
import io.github.vladimirmi.internetradioplayer.data.utils.ShortcutHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class StationInteractor
@Inject constructor(private val stationRepository: StationRepository,
                    private val groupListRepository: GroupListRepository,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val shortcutHelper: ShortcutHelper) {

    val stationObs: Observable<Station> get() = stationRepository.stationObs
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
                    val favoriteStation = groupListRepository.stations.findStation { it.uri == newStation.uri }
                    station = favoriteStation ?: newStation
                }
    }

    fun addToFavorite(): Completable {
        val newStation = station.copy(order = groupListRepository.stations.size)
        return stationRepository.addToFavorite(newStation)
                .andThen(favoriteListInteractor.initFavoriteList())
                .andThen({ station = newStation }.toCompletable())
    }

    fun removeFromFavorite(): Completable {
        return stationRepository.removeFromFavorite(station)
                .andThen(favoriteListInteractor.initFavoriteList())
                .andThen({ station = station }.toCompletable())
    }

    fun changeGroup(groupName: String): Completable {
        return Single.just(groupListRepository.groups.find { it.name == groupName })
                .flatMapCompletable {
                    if (it.id == station.groupId) Completable.complete()
                    else {
                        val newStation = station.copy(groupId = it.id)
                        stationRepository.updateStations(listOf(newStation))
                                .andThen({ station = newStation }.toCompletable())
                    }
                }
                .andThen(favoriteListInteractor.initFavoriteList())
    }

    fun editStationTitle(title: String): Completable {
        if (title == station.name) return Completable.complete()
        val newStation = station.copy(name = title)
        return stationRepository.updateStations(listOf(newStation))
                .andThen({ station = newStation }.toCompletable())
                .andThen(favoriteListInteractor.initFavoriteList()) //todo if favorite
    }
}

