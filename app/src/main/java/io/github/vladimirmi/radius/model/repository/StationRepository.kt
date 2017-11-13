package io.github.vladimirmi.radius.model.repository

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.GroupingList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.source.StationSource
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationRepository
@Inject constructor(private val stationSource: StationSource,
                    private val preferences: Preferences) {

    private lateinit var stationList: GroupingList
    val groupedStationList: GroupedList<Station> get() = stationList
    val selected = BehaviorRelay.create<Station>()

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

    fun updateAndSave(station: Station) {
        stationList[indexOfFirst(station)] = station
        stationSource.save(station)
    }


    private fun indexOfFirst(station: Station): Int {
        return stationList.indexOfFirst { it.path == station.path }
    }

    fun parseStation(uri: Uri): Single<Station> {
        return stationSource.parseStation(uri).toSingle()

//        stationSource.parseStation(uri) { station ->
//            if (station != null) {
//                setSelected(station)
//                if (stationList.find { it.path == station.path } != null) return@parseStation
//                stationList.add(station)
//                (groupedStationData as MutableLiveData).value = stationList
//            }
//            cb(station)
//        }
    }
}
