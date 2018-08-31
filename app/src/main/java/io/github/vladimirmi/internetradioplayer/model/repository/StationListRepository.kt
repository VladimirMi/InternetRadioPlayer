package io.github.vladimirmi.internetradioplayer.model.repository

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.model.db.dao.StationDao
import io.github.vladimirmi.internetradioplayer.model.db.entity.Group
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station
import io.github.vladimirmi.internetradioplayer.model.entity.groupedlist.StationsGroupList
import io.github.vladimirmi.internetradioplayer.model.manager.Preferences
import io.github.vladimirmi.internetradioplayer.model.source.StationSource
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationListRepository
@Inject constructor(private val stationSource: StationSource,
                    private val preferences: Preferences,
                    private val dao: StationDao) {

    val stationList = StationsGroupList()
    val currentStation: BehaviorRelay<Station> = BehaviorRelay.create()

    init {
        Single.zip(dao.getAllGroups(), dao.getAllStations(),
                BiFunction { groups: List<Group>, stations: List<Station> ->
                    stationList.apply { init(groups, stations) }
                })
                .toCompletable()
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    fun setCurrentStation(station: Station) {
        val pos = stationList.positionOfFirst(station.id)
//        currentStation.accept(stationList[pos])
        currentStation.accept(station)
        preferences.currentPos = pos
    }

    fun createStation(uri: Uri): Single<Station> =
            Single.fromCallable { stationSource.parseStation(uri) }

    fun updateStation(station: Station): Completable {
        return Completable.fromCallable {
            //            stationList[stationList.indexOfFirst { it.id == newStation.id }] = newStation
            dao.update(station)
        }
    }

    fun addStation(station: Station): Completable {
        return Completable.fromCallable {
            dao.insert(station)
        }
    }

    fun removeStation(station: Station): Completable {
        return Completable.fromCallable {
            dao.delete(station)
        }
    }

    fun updateGroup(group: Group): Completable {
        TODO("not implemented")
    }
}
