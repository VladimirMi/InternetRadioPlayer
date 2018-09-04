package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

import io.github.vladimirmi.internetradioplayer.model.db.entity.Group
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station
import java.lang.IllegalStateException

/**
 * Created by Vladimir Mikhalev 24.08.2018.
 */

class StationsGroupList : GroupedList {

    private val groups = arrayListOf<Group>()
    private val flatIndex = FlatGroupIndex()
    private var listener: ((GroupedList) -> Unit)? = null

    override val size: Int
        get() = groups.fold(0) { acc, group -> acc + group.items.size }

    override val itemsSize: Int
        get() = flatIndex.itemsSize

    override val overallSize: Int
        get() = flatIndex.size

    fun init(groups: List<Group>, stations: List<Station>) {
        this.groups.addAll(groups)

        val groupBy = stations.groupBy { it.groupId }
        groups.forEach {
            val items = groupBy[it.id] ?: return@forEach
            it.items = items.toMutableList()
        }
        index()
    }

    fun setOnChangeListener(listener: (GroupedList) -> Unit) {
        this.listener = listener
    }

    fun collapseGroup(id: String): Group {
        val group = groups[indexOfGroup(id)]
        group.expanded = false
        index()
        return group
    }

    fun expandGroup(id: String): Group {
        val group = groups[indexOfGroup(id)]
        group.expanded = true
        index()
        return group
    }

    fun add(group: Group) {
        group.order = groups.size
        groups.add(group)
        index()
    }

    fun add(station: Station) {
        val items = groups[indexOfGroup(station.groupId)].items
        station.order = items.size
        items.add(station)
        index()
    }

    fun removeStation(id: String): Station {
        for (group in groups) {
            for (item in group.items) {
                if (item.id == id) {
                    group.items.remove(item)
                    index()
                    return item
                }
            }
        }
        throw IllegalStateException("Can not find station with id $id")
    }

    private fun index() {
        flatIndex.index(groups)
        listener?.invoke(this)
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
        return groups[index.groupIdx].items[index.itemIdx!!]
    }

    override fun getGroupItemById(id: String): Station? {
        val index = flatIndex.getIndexById(id)
        if (index == null || index.isGroup()) return null
        return groups[index.groupIdx].items[index.itemIdx!!]
    }

    override fun isGroupExpanded(id: String): Boolean {
        return groups[indexOfGroup(id)].expanded
    }

    override fun getPreviousFrom(id: String): Station? {
        val index = flatIndex.getPreviousItemIndex(id)
        return index?.run { groups[groupIdx].items[itemIdx!!] }
    }

    override fun getNextFrom(id: String): Station? {
        val index = flatIndex.getNextItemIndex(id)
        return index?.run { groups[groupIdx].items[itemIdx!!] }
    }

    override fun positionOfFirst(id: String): Int {
        return flatIndex.positionOfFirst(id)
    }

    override fun contains(predicate: (Station) -> Boolean): Boolean {
        return groups.any { it.items.any(predicate) }
    }

//endregion

    fun isFirstStation(id: String): Boolean {
        return (isGroup(0) && positionOfFirst(id) == 1) || positionOfFirst(id) == 0
    }
}
