package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

import io.github.vladimirmi.internetradioplayer.model.db.entity.Group

/**
 * Created by Vladimir Mikhalev 24.08.2018.
 */

class FlatGroupIndex {

    private var flatIndex = ArrayList<GroupIndex>()

    val size: Int
        get() = flatIndex.size
    val itemsSize: Int
        get() = flatIndex.count { !it.isGroup() }

    fun index(groupList: List<Group>) {
        groupList.forEachIndexed { groupIndex, group ->
            flatIndex.add(GroupIndex(group.id, groupIndex, null))
            if (group.expanded) {
                flatIndex.addAll(group.items.mapIndexed { index, element ->
                    GroupIndex(element.id, groupIndex, index)
                })
            }
        }
    }

    fun isGroupTitle(position: Int) = flatIndex[position].isGroup()

    fun getIndex(position: Int) = flatIndex[position]

    fun getIndexById(id: Int): GroupIndex? {
        return flatIndex.find { it.id == id }
    }

    fun getPreviousItemIndex(id: Int): GroupIndex? {
        var previous: GroupIndex? = null
        for (index in flatIndex) {
            if (index.isGroup()) continue
            if (index.id == id && previous != null) return previous
            previous = index
        }
        return previous ?: flatIndex.lastOrNull { !it.isGroup() }
    }

    fun getNextItemIndex(id: Int): GroupIndex? {
        var next: GroupIndex? = null
        for (i in flatIndex.size - 1 downTo 0) {
            val index = flatIndex[i]
            if (index.isGroup()) continue
            if (index.id == id && next != null) return next
            next = index
        }
        return next ?: flatIndex.firstOrNull { !it.isGroup() }
    }

    fun positionOfFirst(id: Int): Int {
        return flatIndex.indexOfFirst { it.id == id }
    }
}

class GroupIndex(val id: Int, val groupIdx: Int, val itemIdx: Int?) {

    fun isGroup() = itemIdx == null
}
