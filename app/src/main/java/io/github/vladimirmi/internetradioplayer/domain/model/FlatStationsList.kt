package io.github.vladimirmi.internetradioplayer.domain.model

import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Vladimir Mikhalev 12.09.2018.
 */

class FlatStationsList(private val flatList: MutableList<Any> = arrayListOf()) : MediaQueue {

    companion object {
        fun createFrom(groups: List<Group>): FlatStationsList {
            val flatList = arrayListOf<Any>()
            groups.forEach { group ->
                if (groups.size > 1) {
                    flatList.add(group)
                }
                if (group.expanded) {
                    flatList.addAll(group.stations)
                }
            }
            return FlatStationsList(flatList)
        }
    }

    val size: Int get() = flatList.size
    override val queueSize: Int get() = getStations().size

    override fun getNext(id: String): Media {
        val stations = getStations()
        val currIndex = stations.indexOfFirst { it.id == id }
        if (currIndex == -1) throw IllegalStateException("Can't find station with id $id")
        return stations[(currIndex + 1) % stations.size]
    }

    override fun getPrevious(id: String): Media {
        val stations = getStations()
        val currIndex = stations.indexOfFirst { it.id == id }
        if (currIndex == -1) throw IllegalStateException("Can't find station with id $id")
        return stations[(stations.size + currIndex - 1) % stations.size]
    }

    fun isGroup(position: Int) = flatList[position] is Group

    fun isStation(position: Int) = flatList[position] is Station

    operator fun get(position: Int): Any = flatList[position]

    fun getGroup(position: Int): Group {
        return flatList[position] as? Group ?: throw IllegalStateException("It is station")
    }

    fun getStation(position: Int): Station {
        return flatList[position] as? Station ?: throw IllegalStateException("It is group")
    }

    fun getId(position: Int): String {
        return if (isGroup(position)) getGroup(position).id else getStation(position).id
    }

    fun isLastStationInGroup(position: Int): Boolean {
        if (position > flatList.size - 1 || isGroup(position)) return false
        return position == flatList.size - 1 || isGroup(position + 1)
    }

    fun findStation(predicate: (Station) -> Boolean): Station? {
        return flatList.find { it is Station && predicate(it) } as? Station
    }

    fun positionOfStation(id: String): Int {
        return flatList.indexOfFirst { it is Station && it.id == id }
    }

    fun moveItem(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                Collections.swap(flatList, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(flatList, i, i - 1)
            }
        }
    }

    fun startMove(position: Int): FlatStationsList {
        return if (isGroup(position)) {
            FlatStationsList(getGroups().map { it.apply { stations = emptyList() } }.toMutableList())
        } else {
            FlatStationsList(ArrayList(flatList))
        }
    }

    fun endMove(): FlatStationsList {
        var stationOrder = 0
        var groupOrder = 0
        var groupId = (flatList.find { it is Group } as? Group)?.id
                ?: (flatList.find { it is Station } as? Station)?.groupId ?: Group.DEFAULT_ID

        flatList.forEachIndexed { index, item ->
            if (item is Group) {
                flatList[index] = item.copy(order = groupOrder)
                groupOrder++
                if (item.id != groupId) stationOrder = 0
                groupId = item.id
            } else if (item is Station) {
                flatList[index] = item.copy(order = stationOrder, groupId = groupId)
                stationOrder++
            }
        }
        return this
    }

    fun getGroupDifference(stations: FlatStationsList): List<Group> {
        val diff = arrayListOf<Group>()
        val other = getGroups()
        stations.getGroups().forEachIndexed { index, group ->
            if (group != other[index]) diff.add(group)
        }
        return diff
    }

    fun getStationDifference(stations: FlatStationsList): List<Station> {
        val diff = arrayListOf<Station>()
        val other = getStations()
        stations.getStations().forEachIndexed { index, station ->
            if (station != other[index]) diff.add(station)
        }
        return diff
    }

    private fun getGroups() = flatList.filterIsInstance(Group::class.java)
    private fun getStations() = flatList.filterIsInstance(Station::class.java)
}
