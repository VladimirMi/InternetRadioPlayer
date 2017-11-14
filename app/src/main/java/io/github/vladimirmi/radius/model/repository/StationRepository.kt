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
    val selected: BehaviorRelay<Station> = BehaviorRelay.create<Station>()

    fun initStations() {
        stationList = GroupingList(stationSource.getStationList())
        if (stationList.size > preferences.selectedPos) {
            selected.accept(stationList[preferences.selectedPos])
        }

    }

    fun setSelected(station: Station) {
        val pos = indexOfFirst(station)
        selected.accept(stationList[pos])
        preferences.selectedPos = pos
    }

    fun parseStation(uri: Uri): Maybe<Station> {
        Maybe.fromCallable { }
        return { stationSource.parseStation(uri) }
                .toMaybe()
                .subscribeOn(Schedulers.io())
    }

    fun update(station: Station) {
        stationList[indexOfFirst(station)] = station
        save(station)
    }

    fun add(station: Station): Boolean {
        val added = stationList.add(station)
        if (added) save(station)
        return added
    }

    private fun save(station: Station) {
        stationSource.save(station)
    }

    private fun indexOfFirst(station: Station) = stationList.indexOfFirst { it.path == station.path }
}
