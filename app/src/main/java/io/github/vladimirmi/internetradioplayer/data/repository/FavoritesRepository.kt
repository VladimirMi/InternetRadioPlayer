package io.github.vladimirmi.internetradioplayer.data.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class FavoritesRepository
@Inject constructor(private val db: StationsDatabase) {

    private val dao = db.stationDao()

    val stationsListObs = BehaviorRelay.create<FlatStationsList>()

    var groups: List<Group> = emptyList()
        private set

    var stations: FlatStationsList
        get() = stationsListObs.value ?: FlatStationsList()
        private set(value) = stationsListObs.accept(value)


    fun initStationsList(groups: List<Group>): Completable {
        return Completable.fromAction {
            this.groups = groups
            this.stations = FlatStationsList.createFrom(groups)
        }
    }

    fun getAllGroups(): Single<List<Group>> {
        return dao.getAllGroups().subscribeOn(Schedulers.io())
    }

    fun addGroup(group: Group): Completable {
        return Completable.fromCallable {
            dao.insertGroup(group)
        }.subscribeOn(Schedulers.io())
    }

    fun removeGroup(group: Group): Completable {
        return Completable.fromCallable {
            dao.deleteGroup(group.id)
        }.subscribeOn(Schedulers.io())
    }

    fun updateGroups(groups: List<Group>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                groups.forEach { dao.updateGroup(it) }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun getAllStations(): Single<List<Station>> {
        return dao.getFavoriteStations()
                .subscribeOn(Schedulers.io())
    }

    fun getStation(predicate: (Station) -> Boolean): Station? {
        for (it in groups) {
            val station = it.stations.find(predicate)
            if (station != null) return station
        }
        return null
    }

    fun addStation(station: Station): Completable {
        return Completable.fromCallable {
            dao.insertStation(station)
        }.subscribeOn(Schedulers.io())
    }

    fun removeStation(station: Station): Completable {
        return Completable.fromCallable {
            dao.deleteStation(station.id)
        }.subscribeOn(Schedulers.io())
    }

    fun updateStations(stations: List<Station>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                stations.forEach { dao.updateStation(it) }
            }
        }.subscribeOn(Schedulers.io())
    }
}
