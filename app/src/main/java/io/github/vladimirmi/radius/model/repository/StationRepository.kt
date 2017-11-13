package io.github.vladimirmi.radius.model.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.GroupingList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.source.StationSource
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationRepository
@Inject constructor(private val stationSource: StationSource,
                    private val preferences: Preferences) {

    private lateinit var stationList: GroupingList
    val selectedData: LiveData<Int> = MutableLiveData()
    val groupedStationData: LiveData<GroupedList<Station>> = MutableLiveData()
    val groupedStationList: GroupedList<Station> get() = stationList

    fun initStations() {
        stationList = GroupingList(stationSource.getStationList())
        (groupedStationData as MutableLiveData).value = stationList
        if (stationList.size > preferences.selectedPos) {
            setSelected(preferences.selectedPos)
        }
    }

    fun setSelected(station: Station) {
        val pos = indexOfFirst(station)
        (selectedData as MutableLiveData).value = pos
        preferences.selectedPos = pos
    }

    fun getSelected(): Station? = selectedData.value?.let { stationList[it] }

    fun updateAndSave(station: Station) {
        update(station)
        save(station)
    }

    private fun update(station: Station) {
        stationList[indexOfFirst(station)] = station
        (groupedStationData as MutableLiveData).value = stationList
    }

    private fun save(station: Station) {
        stationSource.save(station)
    }

    private fun setSelected(pos: Int) {
        (selectedData as MutableLiveData).value = pos
    }

    private fun indexOfFirst(station: Station): Int {
        return stationList.indexOfFirst { it.path == station.path }
    }

    fun addStation(uri: Uri, cb: (Station?) -> Unit) {
        stationSource.fromUri(uri) { station ->
            if (station != null) {
                setSelected(station)
                if (stationList.find { it.path == station.path } != null) return@fromUri
                stationList.add(station)
                (groupedStationData as MutableLiveData).value = stationList
            }
            cb(station)
        }
    }
}
