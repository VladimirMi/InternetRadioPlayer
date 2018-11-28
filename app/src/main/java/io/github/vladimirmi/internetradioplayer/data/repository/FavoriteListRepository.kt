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
    var groups: List<Group> = ArrayList()
        private set
    private var list: FlatStationsList
        get() = _stationsListObs.value!!
        set(value) = _stationsListObs.accept(value)

    fun initStationsList(groups: List<Group>): Completable {
        return Completable.fromAction {
            this.groups = groups
            this.list = FlatStationsList.createFrom(groups)
        }
    }

    fun findStation(predicate: (Station) -> Boolean): Station? {
        return list.findStation(predicate)
    }

    fun findGroup(predicate: (Group) -> Boolean): Group? {
        return groups.find(predicate)
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

    fun updateStations(stations: List<Station>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                stations.forEach { dao.updateStation(it) }
            }
        }
    }
}
