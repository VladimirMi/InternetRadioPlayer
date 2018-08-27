package io.github.vladimirmi.internetradioplayer.model.repository

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.model.entity.Filter
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.model.entity.groupedlist.StationsGroupList
import io.github.vladimirmi.internetradioplayer.model.manager.Preferences
import io.github.vladimirmi.internetradioplayer.model.source.StationSource
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationListRepository
@Inject constructor(private val stationSource: StationSource,
                    private val preferences: Preferences) {

    @Volatile var isInitialized = false
    val stationList: StationsGroupList
    val currentStation: BehaviorRelay<Station> = BehaviorRelay.createDefault(Station.nullObject())
    private val lock = ReentrantLock()

    init {
        val stations = stationSource.getStationList()
        stationList = StationsGroupList(stations)
//        if (stations.isNotEmpty()) {
//            stationList.addAll(stations)
//            stationList.filter(Filter.valueOf(preferences.filter))
//            preferences.hidedGroups.forEach { stationList.collapseGroup(it) }
//            currentStation.accept(stationList[preferences.currentPos])
//        }
    }

    fun initStations() {
        lock.withLock {
            if (isInitialized) return
            isInitialized = true
        }
    }

    fun setCurrentStation(station: Station) {
        val pos = stationList.positionOfFirst(station.id)
//        currentStation.accept(stationList[pos])
        currentStation.accept(station)
        preferences.currentPos = pos
    }

    fun createStation(uri: Uri): Single<Station> =
            Single.fromCallable { stationSource.parseStation(uri) }

    fun updateStation(newStation: Station): Completable {
        return Completable.fromCallable {
            //            stationList[stationList.indexOfFirst { it.id == newStation.id }] = newStation
            saveStation(newStation)
        }
    }

    fun addStation(station: Station): Completable {
        return Completable.fromCallable {
            stationList.add(station)
            saveStation(station)
        }
    }

    fun removeStation(station: Station): Completable {
        return Completable.fromCallable {
            stationList.remove(station)
            stationSource.removeStation(station)
        }
    }

    fun showGroup(group: String) {
        stationList.expandGroup(group)
        preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { remove(group) }
    }

    fun hideGroup(group: String) {
        stationList.collapseGroup(group)
        preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { add(group) }
    }

    private fun saveStation(station: Station) {
        stationSource.saveStation(station)
    }

    fun filterStations(filter: Filter) {
//        stationList.filter(filter)
        preferences.filter = filter.name
    }
}
