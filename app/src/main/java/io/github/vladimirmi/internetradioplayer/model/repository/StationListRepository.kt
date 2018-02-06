package io.github.vladimirmi.internetradioplayer.model.repository

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.model.entity.Filter
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.model.entity.groupedlist.GroupingList
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
    val stationList: GroupingList = GroupingList()
    val currentStation: BehaviorRelay<Station> = BehaviorRelay.create()
    private val lock = ReentrantLock()

    fun initStations() {
        lock.withLock {
            if (isInitialized) return
            stationList.addAll(stationSource.getStationList())
            stationList.filter(Filter.valueOf(preferences.filter))
            preferences.hidedGroups.forEach { stationList.hideGroup(it) }
            currentStation.accept(stationList[preferences.currentPos])
            isInitialized = true
        }
    }

    fun setCurrentStation(station: Station) {
        val pos = stationList.indexOfFirst { it.id == station.id }
        currentStation.accept(stationList[pos])
        preferences.currentPos = pos
    }

    fun createStation(uri: Uri): Single<Station> =
            Single.fromCallable { stationSource.parseStation(uri) }

    fun updateStation(newStation: Station): Completable {
        return Completable.fromCallable {
            stationList[stationList.indexOfFirst { it.id == newStation.id }] = newStation
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
        stationList.showGroup(group)
        preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { remove(group) }
    }

    fun hideGroup(group: String) {
        stationList.hideGroup(group)
        preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { add(group) }
    }

    private fun saveStation(station: Station) {
        stationSource.saveStation(station)
    }

    fun filterStations(filter: Filter) {
        stationList.filter(filter)
        preferences.filter = filter.name
    }
}
