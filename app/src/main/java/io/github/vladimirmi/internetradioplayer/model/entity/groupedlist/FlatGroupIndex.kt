package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

import io.github.vladimirmi.internetradioplayer.model.entity.Station

/**
 * Created by Vladimir Mikhalev 24.08.2018.
 */

class FlatGroupIndex {

    private var flatIndex = ArrayList<GroupIndex>()

    val size: Int
        get() = flatIndex.size

    fun index(groupList: List<Group<Station>>) {
        groupList.forEachIndexed { groupIndex, group ->
            flatIndex.add(GroupIndex(group.id, groupIndex, null))
            if (group.expanded) {
                flatIndex.addAll(group.elements.mapIndexed { index, element ->
                    GroupIndex(element.id, groupIndex, index)
                })
            }
        }
    }

    fun isGroupTitle(position: Int) = flatIndex[position].isGroupTitle()

    fun getIndex(position: Int) = flatIndex[position]

    fun getIndex(id: String): GroupIndex? {
        return flatIndex.find { it.id == id }
    }

    fun getPreviousItemIndex(id: String): GroupIndex? {
        var previous: GroupIndex? = null
        for (index in flatIndex) {
            if (index.isGroupTitle()) continue
            if (index.id == id && previous != null) return previous
            previous = index
        }
        return previous ?: flatIndex.lastOrNull { !it.isGroupTitle() }
    }

    fun getNextItemIndex(id: String): GroupIndex? {
        var next: GroupIndex? = null
        for (i in flatIndex.size - 1 downTo 0) {
            val index = flatIndex[i]
            if (index.isGroupTitle()) continue
            if (index.id == id && next != null) return next
            next = index
        }
        return next ?: flatIndex.firstOrNull { !it.isGroupTitle() }
    }

    fun positionOfFirst(id: String): Int {
        return flatIndex.indexOfFirst { it.id == id }
    }
}

class GroupIndex(val id: String, val groupIdx: Int, val itemIdx: Int?) {

    fun isGroupTitle() = itemIdx == null
}
