package io.github.vladimirmi.internetradioplayer.domain.model

import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import java.util.*


/**
 * Created by Vladimir Mikhalev 12.09.2018.
 */

class FlatStationsList(private val flatList: MutableList<Any> = arrayListOf()) {

    val size: Int get() = flatList.size

    companion object {
        fun createFrom(groups: List<Group>): FlatStationsList {
            val flatList = arrayListOf<Any>()
            groups.forEach { group ->
                if (group.stations.isNotEmpty() && groups.count { it.stations.isNotEmpty() } > 1) {
                    flatList.add(group)
                }
                if (group.expanded) {
                    flatList.addAll(group.stations)
                }
            }
            return FlatStationsList(flatList)
        }
    }

    fun isGroup(position: Int) = flatList[position] is Group

    fun isStation(position: Int) = flatList[position] is Station

    fun getGroup(position: Int): Group {
        return flatList[position] as? Group ?: throw IllegalStateException("It is station")
    }

    fun getStation(position: Int): Station {
        return flatList[position] as? Station ?: throw IllegalStateException("It is group")
    }

    fun getId(position: Int): String {
        return if (isGroup(position)) getGroup(position).id else getStation(position).id
    }

    fun getPreviousFrom(id: String): Station? {
        var previous: Station? = null
        for (element in flatList) {
            if (element is Group) continue
            element as Station
            if (element.id == id && previous != null) return previous
            previous = element
        }
        previous = previous ?: flatList.lastOrNull { it is Station } as? Station
        return if (previous?.id == id) null else previous
    }

    fun getNextFrom(id: String): Station? {
        var next: Station? = null
        for (i in flatList.size - 1 downTo 0) {
            val element = flatList[i]
            if (element is Group) continue
            element as Station
            if (element.id == id && next != null) return next
            next = element
        }
        next = next ?: flatList.firstOrNull { it is Station } as? Station
        return if (next?.id == id) null else next
    }

    fun isFirstStation(id: String): Boolean {
        return (isGroup(0) && isStation(1) && getStation(1).id == id) || (isStation(0) && getStation(0).id == id)
    }

    fun isLastStationInGroup(position: Int): Boolean {
        if (position > flatList.size - 1 || isGroup(position)) return false
        return position == flatList.size - 1 || isGroup(position + 1)
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
        if (isGroup(position)) {
            return FlatStationsList(flatList.asSequence().filterIsInstance(Group::class.java)
                    .toMutableList())
        }
        return this
    }

    fun endMove() {
        var stationOrder = 0
        var groupOrder = 0
        var groupId = (flatList.find { it is Group } as? Group)?.id ?: Group.DEFAULT_ID

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
    }

    fun getGroupUpdatesFrom(stations: FlatStationsList): List<Group> {
        val updates = arrayListOf<Group>()
        val other = getGroups()
        stations.getGroups().forEachIndexed { index, group ->
            if (group != other[index]) updates.add(group)
        }
        return updates
    }

    fun getStationUpdatesFrom(stations: FlatStationsList): List<Station> {
        val updates = arrayListOf<Station>()
        val other = getStations()
        stations.getStations().forEachIndexed { index, station ->
            if (station != other[index]) updates.add(station)
        }
        return updates
    }

    fun getFirstStation(): Station? = flatList.firstOrNull { it is Station } as? Station

    fun haveStations() = flatList.any { it is Station }

    private fun getGroups() = flatList.filterIsInstance(Group::class.java)
    private fun getStations() = flatList.filterIsInstance(Station::class.java)
}
