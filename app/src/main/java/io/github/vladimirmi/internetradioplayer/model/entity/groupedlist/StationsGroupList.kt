package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

import io.github.vladimirmi.internetradioplayer.extensions.then
import io.github.vladimirmi.internetradioplayer.model.db.entity.Group
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station
import io.reactivex.Completable
import java.lang.IllegalStateException

/**
 * Created by Vladimir Mikhalev 24.08.2018.
 */

//todo make interactor from this class
class StationsGroupList : GroupedList {

    private lateinit var listener: StationsGroupListener
    private lateinit var changeListener: (StationsGroupList) -> Unit

    private val groups = arrayListOf<Group>()
    private val flatIndex = FlatGroupIndex()

    override val size: Int
        get() = groups.fold(0) { acc, group -> acc + group.stations.size }

    override val itemsSize: Int
        get() = flatIndex.itemsSize

    override val overallSize: Int
        get() = flatIndex.size

    fun init(groups: List<Group>, stations: List<Station>,
             listener: StationsGroupListener, changeListener: (StationsGroupList) -> Unit) {
        this.listener = listener
        this.changeListener = changeListener
        this.groups.addAll(groups)

        val groupBy = stations.groupBy { it.groupId }
        groups.forEach { group ->
            val items = groupBy[group.id] ?: return@forEach
            items.forEach { it.group = group.name }
            group.stations = items.toMutableList()
        }
        index()
    }

    fun collapseGroup(id: String): Completable {
        val i = indexOfGroup(id)
        val group = groups[i].copy(expanded = false)
        return listener.onGroupUpdate(listOf(group))
                .doOnComplete {
                    groups[i] = group
                    index()
                }
    }

    fun expandGroup(id: String): Completable {
        val i = indexOfGroup(id)
        val group = groups[i].copy(expanded = true)
        return listener.onGroupUpdate(listOf(group))
                .then {
                    groups[i] = group
                    index()
                }
    }

    fun add(group: Group): Completable {
        val newGroup = group.copy(order = groups.size)
        return listener.onGroupAdd(newGroup)
                .then {
                    groups.add(newGroup)
                    index()
                }
    }

    fun add(station: Station): Completable {
        val addGroup = if (indexOfGroup(station.groupId) == -1) {
            val group = Group(station.groupId,
                    if (station.groupId == Group.DEFAULT_ID) Group.DEFAULT_NAME else station.group,
                    groups.size)
            add(group)
        } else Completable.complete()

        var stations: MutableList<Station> = arrayListOf()
        var newStation: Station = station

        return addGroup.doOnComplete {
            stations = groups[indexOfGroup(station.groupId)].stations
            newStation = station.copy(order = stations.size)
        }.andThen(listener.onStationAdd(newStation))
                .then {
                    stations.add(newStation)
                    index()
                }
    }

    fun removeStation(id: String, groupId: String): Completable {
        val group = groups.find { it.id == groupId }
                ?: return Completable.error(IllegalStateException("Can not find group with id $groupId"))
        val station = group.stations.find { it.id == id }
                ?: return Completable.error(IllegalStateException("Can not find station with id $id"))

        return listener.onStationRemove(station)
                .then {
                    group.stations.remove(station)
                    index()
                }
    }

    private fun index() {
        flatIndex.index(groups)
        changeListener.invoke(this)
    }

    private fun indexOfGroup(groupId: String): Int {
        return groups.indexOfFirst { it.id == groupId }
    }

    //todo remove not used from interface
    //region =============== GroupedList ==============

    override fun isGroup(position: Int): Boolean {
        return flatIndex.isGroupTitle(position)
    }

    override fun getGroup(position: Int): Group {
        val index = flatIndex.getIndex(position)
        if (!index.isGroup()) throw IllegalStateException("It is group item")
        return groups[index.groupIdx]
    }

    override fun getGroupItem(position: Int): Station {
        val index = flatIndex.getIndex(position)
        if (index.isGroup()) throw IllegalStateException("It is group title")
        return groups[index.groupIdx].stations[index.itemIdx!!]
    }

    override fun getGroupItemById(id: String): Station? {
        val index = flatIndex.getIndexById(id)
        if (index == null || index.isGroup()) return null
        return groups[index.groupIdx].stations[index.itemIdx!!]
    }

    override fun isGroupExpanded(id: String): Boolean {
        return groups[indexOfGroup(id)].expanded
    }

    override fun getPreviousFrom(id: String): Station? {
        val index = flatIndex.getPreviousItemIndex(id)
        return index?.run { groups[groupIdx].stations[itemIdx!!] }
    }

    override fun getNextFrom(id: String): Station? {
        val index = flatIndex.getNextItemIndex(id)
        return index?.run { groups[groupIdx].stations[itemIdx!!] }
    }

    override fun positionOfFirst(id: String): Int {
        return flatIndex.positionOfFirst(id)
    }

    override fun contains(predicate: (Station) -> Boolean): Boolean {
        return groups.any { it.stations.any(predicate) }
    }

//endregion

    fun isFirstStation(id: String): Boolean {
        return (isGroup(0) && positionOfFirst(id) == 1) || positionOfFirst(id) == 0
    }
}

interface StationsGroupListener {
    fun onGroupAdd(group: Group): Completable
    fun onGroupRemove(group: Group): Completable
    fun onGroupUpdate(groups: List<Group>): Completable

    fun onStationAdd(station: Station): Completable
    fun onStationRemove(station: Station): Completable
    fun onStationUpdate(stations: List<Station>): Completable
}
