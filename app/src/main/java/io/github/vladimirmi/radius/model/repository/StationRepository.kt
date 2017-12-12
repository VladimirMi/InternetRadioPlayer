package io.github.vladimirmi.radius.model.repository

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.radius.extensions.toMaybe
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.GroupingList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.source.StationSource
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationRepository
@Inject constructor(private val stationSource: StationSource,
                    private val preferences: Preferences) {

    private lateinit var stationList: GroupingList
    val groupedStationList: GroupedList<Station> get() = stationList
    val current: BehaviorRelay<Station> = BehaviorRelay.create<Station>()
    var newStation: Station? = null

    fun initStations() {
        stationList = GroupingList(stationSource.getStationList())
        if (stationList.size > preferences.selectedPos) {
            current.accept(stationList[preferences.selectedPos])
        }
    }

    fun setCurrent(station: Station) {
        val pos = stationList.indexOf(station)
        current.accept(stationList[pos])
        preferences.selectedPos = pos
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

    fun update(station: Station) {
        if (station.id == current.value.id) current.accept(station)
        stationList[stationList.indexOfFirst { it.id == station.id }] = station
        stationSource.save(station)
    }

    fun add(station: Station): Boolean {
        if (stationList.find { it.title == station.title } != null) return false
        stationList.add(station)
        stationSource.save(station)
        setCurrent(station)
        return true
    }

    fun remove(station: Station) {
        if (stationList.remove(station)) {
            stationSource.remove(station)
        }
    }

    fun next(): Boolean {
        val next = stationList.getNext(current.value)
        return if (next != null) {
            setCurrent(next)
            true
        } else false
    }

    fun previous(): Boolean {
        val previous = stationList.getPrevious(current.value)
        return if (previous != null) {
            setCurrent(previous)
            true
        } else false
    }
}
