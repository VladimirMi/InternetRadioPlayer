package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

import io.github.vladimirmi.internetradioplayer.model.entity.Station
import java.lang.IllegalStateException

/**
 * Created by Vladimir Mikhalev 24.08.2018.
 */

class StationsGroupList(private val groups: List<Group<Station>>,
                        private val stations: List<Station>) : GroupedList<Station> {

    private val flatIndex = FlatGroupIndex()
    private var listener: OnChangeListener? = null

    override val size: Int
        get() = TODO("not implemented")

    override val itemsSize: Int
        get() = TODO("not implemented")

    override val overallSize: Int
        get() = flatIndex.size

    init {
        val groupBy = stations.groupBy { it.group }
        groups.forEach {
            val elements = groupBy[it.id] ?: return@forEach
            it.elements = elements.toMutableList()
        }
        flatIndex.index(groups)
    }

    fun setOnChangeListener(listener: OnChangeListener) {
        this.listener = listener
    }

    fun collapseGroup(id: String) {
        val group = groups[indexOfGroup(id)]
        group.expanded = false
        flatIndex.index(groups)
        listener?.onGroupsChange(listOf(group))
    }

    fun expandGroup(id: String) {
        val group = groups[indexOfGroup(id)]
        group.expanded = true
        flatIndex.index(groups)
        listener?.onGroupsChange(listOf(group))
    }

    fun add(element: Station) {
        val elements = groups[indexOfGroup(element.group)].elements
        elements.add(element)
        flatIndex.index(groups)
        listener?.onItemsChange(elements)
    }

    fun remove(element: Station) {
        val elements = groups[indexOfGroup(element.group)].elements
        elements.remove(element)
        flatIndex.index(groups)
        listener?.onItemsChange(elements)
    }

    private fun indexOfGroup(title: String): Int {
        return groups.indexOfFirst { it.title == title }
    }

    //region =============== GroupedList ==============

    override fun isGroupTitle(position: Int): Boolean {
        return flatIndex.isGroupTitle(position)
    }

    override fun getGroupTitle(position: Int): String {
        val index = flatIndex.getIndex(position)
        return groups[index.groupIdx].title
    }

    override fun getGroupItem(position: Int): Station {
        val index = flatIndex.getIndex(position)
        if (index.isGroupTitle()) throw IllegalStateException("It is group title")
        return groups[index.groupIdx].elements[index.itemIdx!!]
    }

    override fun getGroupItem(id: String): Station? {
        val index = flatIndex.getIndex(id)
        if (index == null || index.isGroupTitle()) return null
        return groups[index.groupIdx].elements[index.itemIdx!!]
    }

    override fun isGroupExpanded(group: String): Boolean {
        return groups[indexOfGroup(group)].expanded
    }

    override fun getPrevious(element: Station): Station? {
        val index = flatIndex.getPreviousItemIndex(element.id)
        return index?.run { groups[groupIdx].elements[itemIdx!!] }
    }

    override fun getNext(element: Station): Station? {
        val index = flatIndex.getNextItemIndex(element.id)
        return index?.run { groups[groupIdx].elements[itemIdx!!] }
    }

    override fun positionOfFirst(id: String): Int {
        return flatIndex.positionOfFirst(id)
    }

    override fun contains(predicate: (Station) -> Boolean): Boolean {
        return stations.find(predicate) != null
    }

    //endregion

    interface OnChangeListener {
        fun onGroupsChange(groups: List<Group<Station>>)

        fun onItemsChange(items: List<Station>)
    }

}
