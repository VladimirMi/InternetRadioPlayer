package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.model.entity.Filter
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.reactivex.Observable
import java.lang.IllegalStateException
import java.util.*

/**
 * Created by Vladimir Mikhalev 11.10.2017.
 */

class GroupingList : MutableGroupedList<Station> {

    private val stationList = ArrayList<Station>()
    private val mappings = ArrayList<GroupMapping>()
    private val obs: BehaviorRelay<GroupedList<Station>> = BehaviorRelay.create()

    override var filter: Filter = Filter.DEFAULT

    override fun isGroupTitle(position: Int): Boolean = getVisibleMapping(position) is GroupMapping.Title

    override fun getGroupTitle(position: Int): String = getVisibleMapping(position).group

    override fun getGroupItem(position: Int): Station {
        val mapping = getVisibleMapping(position)
        return when (mapping) {
            is GroupMapping.Title -> throw IllegalStateException("Should call getGroupTitle()")
            is GroupMapping.Item -> stationList[mapping.index]
        }
    }

    override fun get(index: Int): Station {
        return if (index < 0 || index >= size) Station.nullObject()
        else stationList[index]
    }

    override fun isGroupVisible(group: String): Boolean {
        return mappings.find { it.group == group && it is GroupMapping.Item }?.visible == true
    }

    override val size: Int get() = stationList.size

    override val itemsSize: Int get() = mappings.count { it.visible && it is GroupMapping.Item }

    override val overallSize: Int get() = mappings.count { it.visible }

    override fun getPrevious(element: Station, cycle: Boolean): Station? {
        var prev: GroupMapping.Item? = null
        mappings.asSequence()
                .filter { it.visible && it is GroupMapping.Item }
                .forEach {
                    it as GroupMapping.Item
                    if (it.id == element.id && prev != null) {
                        return stationList[prev!!.index]
                    }
                    prev = it
                }
        return if (cycle) prev?.let { stationList[it.index] }
        else element
    }

    override fun getNext(element: Station, cycle: Boolean): Station? {
        var next: GroupMapping.Item? = null
        mappings.reversed().asSequence()
                .filter { it.visible && it is GroupMapping.Item }
                .forEach {
                    it as GroupMapping.Item
                    if (it.id == element.id && next != null) {
                        return stationList[next!!.index]
                    }
                    next = it
                }
        return if (cycle) next?.let { stationList[it.index] }
        else element
    }

    override fun observe(): Observable<GroupedList<Station>> =
            obs.apply { accept(this@GroupingList) }

    override fun contains(element: Station): Boolean {
        return mappings.filterIsInstance(GroupMapping.Item::class.java)
                .any { it.id == element.id }
    }

    override fun firstOrNull(predicate: (Station) -> Boolean): Station? {
        return mappings.filterIsInstance(GroupMapping.Item::class.java)
                .map { stationList[it.index] }
                .find(predicate)
    }

    override fun haveItems(predicate: (Station) -> Boolean): Boolean {
        return stationList.any(predicate)
    }

    override fun positionOfFirst(predicate: (Station) -> Boolean): Int {
        val station = firstOrNull(predicate) ?: return -1
        return mappings.filter { it.visible && it is GroupMapping.Item }
                .indexOfFirst { (it as GroupMapping.Item).id == station.id }
    }

    override fun canFilter(filter: Filter): Boolean {
        if (this.filter == filter) return false
        val count = stationList.count(filter.predicate)
        return count > 0 && count != itemsSize
    }

    override fun indexOfFirst(predicate: (Station) -> Boolean): Int {
        return stationList.indexOfFirst(predicate)
    }

    //region =============== MutableGroupedList ==============

    override fun hideGroup(group: String) {
        mappings.forEach {
            if (it.group == group && it is GroupMapping.Item) it.visible = false
        }
        notifyObservers()
    }

    override fun showGroup(group: String) {
        mappings.forEach {
            if (it.group == group && it is GroupMapping.Item) it.visible = true
        }
        notifyObservers()
    }

    override fun addAll(elements: Collection<Station>): Boolean {
        if (stationList.isNotEmpty()) stationList.clear()
        return stationList.addAll(elements).also {
            initMappings()
        }
    }

    override fun add(element: Station): Boolean {
        return stationList.add(element).also {
            initMappings()
        }
    }

    override operator fun set(index: Int, element: Station): Station {
        return stationList.set(index, element).also {
            initMappings()
        }
    }

    override fun remove(element: Station): Boolean {
        val removed = stationList.remove(element)
        if (removed) {
            initMappings()
        }
        return removed
    }

    override fun filter(filter: Filter) {
        this.filter = filter
        initMappings()
    }

    //endregion

    private fun initMappings() {
        stationList.sortBy { it.name }
        stationList.sortBy { it.group }
        createMappings()
        if (stationList.isNotEmpty() && itemsSize == 0) filter(Filter.DEFAULT)
        notifyObservers()
    }

    private fun createMappings() {
        mappings.clear()
        stationList.forEachIndexed { index, station ->
            if (!filter.predicate.invoke(station)) return@forEachIndexed
            if ((mappings.isEmpty() || mappings.last().group != station.group) && station.group.isNotBlank()) {
                mappings.add(GroupMapping.Title(station.group))
            }
            mappings.add(GroupMapping.Item(station.group, station.id, index))
        }
    }

    private fun notifyObservers() {
        obs.accept(this)
    }

    private fun getVisibleMapping(position: Int): GroupMapping {
        var count = 0
        mappings.forEach { groupMapping ->
            if (groupMapping.visible) {
                if (count == position) return groupMapping
                count++
            }
        }
        throw IllegalStateException()
    }

    override fun toString(): String {
        return mappings.toString()
    }
}
