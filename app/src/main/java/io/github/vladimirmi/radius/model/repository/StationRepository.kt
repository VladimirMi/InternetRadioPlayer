package io.github.vladimirmi.radius.model.repository

import android.graphics.Bitmap
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
    val selected: BehaviorRelay<Station> = BehaviorRelay.create<Station>()
    lateinit var iconBitmap: Bitmap
    var newStation: Station? = null

    fun initStations() {
        //todo sort before adding
        stationList = GroupingList(stationSource.getStationList())
        if (stationList.size > preferences.selectedPos) {
            selected.accept(stationList[preferences.selectedPos])
        }
    }

    fun setSelected(station: Station) {
        val pos = stationList.indexOf(station)
        selected.accept(stationList[pos])
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
        if (station.id == selected.value.id) selected.accept(station)
        stationList[stationList.indexOfFirst { it.id == station.id }] = station
        stationSource.save(station)
    }

    fun add(station: Station): Boolean {
        if (stationList.find { it.title == station.title } != null) return false
        stationList.add(station)
        stationSource.save(station)
        setSelected(station)
        return true
    }

    fun remove(station: Station) {
        if (stationList.remove(station)) {
            stationSource.remove(station)
        }
    }

    fun next(): Boolean {
        val next = stationList.getNext(selected.value)
        return if (next != null) {
            setSelected(next)
            true
        } else false
    }

    fun previous(): Boolean {
        val previous = stationList.getPrevious(selected.value)
        return if (previous != null) {
            setSelected(previous)
            true
        } else false
    }
}
