package io.github.vladimirmi.internetradioplayer.domain.interactor

import android.net.Uri
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.FavoriteListRepository
import io.github.vladimirmi.internetradioplayer.data.repository.StationRepository
import io.github.vladimirmi.internetradioplayer.data.utils.ShortcutHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class StationInteractor
@Inject constructor(private val stationRepository: StationRepository,
                    private val favoriteListRepository: FavoriteListRepository,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val shortcutHelper: ShortcutHelper) {

    val stationObs: Observable<Station> get() = stationRepository.stationObs
    val station get() = stationRepository.station

    fun addCurrentShortcut(startPlay: Boolean): Boolean {
        return shortcutHelper.pinShortcut(station, startPlay)
    }

    fun createStation(uri: Uri): Single<Station> {
        return stationRepository.createStation(uri)
                .doOnSuccess { newStation ->
                    val favoriteStation = favoriteListRepository.findStation { it.uri == newStation.uri }
                    stationRepository.station = favoriteStation ?: newStation
                }.subscribeOn(Schedulers.io())
    }

    fun addToFavorite(): Completable {
        return stationRepository.addToFavorite(station)
                .andThen(favoriteListInteractor.initFavoriteList())
                .subscribeOn(Schedulers.io())
    }

    fun removeFromFavorite(): Completable {
        return stationRepository.removeFromFavorite(station)
                .andThen(favoriteListInteractor.initFavoriteList())
                .subscribeOn(Schedulers.io())
    }

    fun changeGroup(groupName: String): Completable {
        return Single.just(favoriteListRepository.findGroup { it.name == groupName })
                .flatMapCompletable { stationRepository.updateStation(station.copy(groupId = it.id)) }
                .andThen(favoriteListInteractor.initFavoriteList())
                .subscribeOn(Schedulers.io())
    }
}

