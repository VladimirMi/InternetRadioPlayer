package io.github.vladimirmi.internetradioplayer.domain.model

import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import java.lang.IllegalStateException

/**
 * Created by Vladimir Mikhalev 12.09.2018.
 */

class FlatStationsList {

    private val flatList = ArrayList<Any>()

    val size: Int
        get() = flatList.size
    val stationsSize: Int
        get() = flatList.count { it is Station }

    fun build(groups: List<Group>) {
        flatList.clear()
        groups.forEach { group ->
            if (!group.isDefault() || groups.size > 1) {
                flatList.add(group)
            }
            if (group.expanded) {
                flatList.addAll(group.stations)
            }
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

    fun getPreviousFrom(id: String): Station? {
        var previous: Station? = null
        for (element in flatList) {
            if (element is Group) continue
            element as Station
            if (element.id == id && previous != null) return previous
            previous = element
        }
        return previous ?: flatList.lastOrNull { it is Station } as Station
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
        return next ?: flatList.firstOrNull { it is Station } as Station
    }

    fun isFirstStation(id: String): Boolean {
        return (isGroup(0) && isStation(1) && getStation(1).id == id) || (isStation(0) && getStation(0).id == id)
    }

    fun positionOfStation(id: String): Int {
        return flatList.indexOfFirst { it is Station && it.id == id }
    }
}
