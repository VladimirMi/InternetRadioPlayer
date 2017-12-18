package io.github.vladimirmi.radius.model.repository

import android.graphics.Bitmap
import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.radius.extensions.toMaybe
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.GroupingList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.source.StationIconSource
import io.github.vladimirmi.radius.model.source.StationSource
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationRepository
@Inject constructor(private val stationSource: StationSource,
                    private val stationIconSource: StationIconSource,
                    private val preferences: Preferences) {

    private lateinit var stationList: GroupingList
    val groupedStationList: GroupedList<Station> get() = stationList
    val currentStation: BehaviorRelay<Station> = BehaviorRelay.create<Station>()
    var newStation: Station? = null

    fun initStations() {
        stationList = GroupingList(stationSource.getStationList())
        if (stationList.size > preferences.selectedPos) {
            currentStation.accept(stationList[preferences.selectedPos])
        }
        preferences.hidedGroups.forEach { stationList.hideGroup(it) }
    }

    fun setCurrent(station: Station) {
        val pos = stationList.indexOf(station)
        currentStation.accept(stationList[pos])
        preferences.currentPos = pos
    }

    fun getStation(id: String): Station =
            stationList.find { it.id == id } ?: throw IllegalStateException()

    fun hasStations(): Boolean = stationList.isNotEmpty()

    fun parseStation(uri: Uri): Maybe<Station> {
        return { stationSource.parseStation(uri) }
                .toMaybe()
                .subscribeOn(Schedulers.io())
                .doOnSuccess { newStation = it }
    }

    fun showOrHideGroup(group: String) {
        if (stationList.isGroupVisible(group)) {
            stationList.hideGroup(group)
            preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { add(group) }
        } else {
            stationList.showGroup(group)
            preferences.hidedGroups = preferences.hidedGroups.toMutableSet().apply { remove(group) }
        }
    }

    fun updateStation(newStation: Station) {
        val oldStation = currentStation.value
        if (oldStation != newStation) {
            stationList[stationList.indexOfFirst { it.id == newStation.id }] = newStation
            stationSource.saveStation(newStation)
            stationIconSource.saveIcon(newStation, getStationIcon(oldStation.title).blockingGet())
            if (oldStation.title != newStation.title) {
                stationSource.removeStation(oldStation)
                stationIconSource.removeIcon(oldStation)
            }
            currentStation.accept(newStation)
        }
    }

    fun saveStationIcon(bitmap: Bitmap) {
        stationIconSource.saveIcon(currentStation.value, bitmap)
    }

    fun addStation(station: Station): Boolean {
        if (stationList.find { it.title == station.title } != null) return false
        stationList.add(station)
        stationSource.saveStation(station)
        setCurrent(station)
        return true
    }

    fun removeStation(station: Station) {
        if (stationList.remove(station)) {
            stationSource.removeStation(station)
        }
    }

    fun nextStation(): Boolean {
        val next = stationList.getNext(currentStation.value)
        return if (next != null) {
            setCurrent(next)
            true
        } else false
    }

    fun previousStation(): Boolean {
        val previous = stationList.getPrevious(currentStation.value)
        return if (previous != null) {
            setCurrent(previous)
            true
        } else false
    }

    fun getStationIcon(path: String): Single<Bitmap> {
        return { stationIconSource.getIcon(path) }
                .toSingle()
                .subscribeOn(Schedulers.io())
    }
}
