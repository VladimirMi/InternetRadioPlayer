package io.github.vladimirmi.internetradioplayer.data.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class FavoriteListRepository
@Inject constructor(private val db: StationsDatabase) {

    private val dao = db.stationDao()

    private val _stationsListObs = BehaviorRelay.createDefault(FlatStationsList())
    val stationsListObs: Observable<FlatStationsList> get() = _stationsListObs
    private val list: FlatStationsList
        get() = _stationsListObs.value!!

    fun initStationsList(): Completable {
        return Completable.complete()
    }

    fun findStation(predicate: (Station) -> Boolean): Station? {
        return list.findStation(predicate)
    }

    fun getAllStations(): Single<List<Station>> {
        return dao.getAllStations()
                .toObservable()
                .flatMapIterable { it }
                .toList()
    }

    fun getAllGroups(): Single<List<Group>> = dao.getAllGroups()


    fun addGroup(group: Group): Completable {
        return Completable.fromCallable {
            dao.insertGroup(group)
        }
    }

    fun removeGroup(group: Group): Completable {
        TODO("not implemented")
    }

    fun updateGroups(groups: List<Group>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                groups.forEach { dao.updateGroup(it) }
            }
        }
    }

    fun addStation(station: Station): Completable {
//        return Completable.fromCallable {
//            val group = dao.getGroupByName(station.groupName)
//            val newStation = if (station.groupId != group.id) {
//                station.copy(groupId = group.id)
//            } else station
//
//            dao.insertStation(newStation)
//            val genres = station.genres.map(::Genre)
//            dao.insertGenres(genres)
//            dao.insertStationGenre(genres.map { StationGenreJoin(station.id, it.name) })
//        }
        return Completable.complete()
    }

    fun removeStation(station: Station): Completable {
        return Completable.fromCallable {
            dao.deleteStation(station.id)
        }
    }

    fun updateStations(stations: List<Station>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                stations.forEach { dao.updateStation(it) }
            }
        }
    }
}
