package io.github.vladimirmi.radius.model.entity

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import java.lang.IllegalStateException

/**
 * Created by Vladimir Mikhalev 11.10.2017.
 */

class GroupingList(private val stationList: ArrayList<Station>)
    : MutableList<Station> by stationList, GroupedList<Station> {

    private val mappings = ArrayList<GroupMapping>()
    private val obs: BehaviorRelay<GroupedList<Station>> = BehaviorRelay.createDefault(this)

    private fun initMappings() {
        mappings.clear()
        stationList.sortBy { it.group }
        stationList.forEachIndexed { index, station ->
            if ((mappings.isEmpty() || mappings.last().group != station.group) && station.group.isNotBlank()) {
                mappings.add(GroupMapping.Title(station.group))
            }
            mappings.add(GroupMapping.Item(station.group, station.id, index))
        }
    }

    override fun isGroupTitle(position: Int): Boolean = getVisibleMapping(position) is GroupMapping.Title

    override fun getGroupTitle(position: Int): String = getVisibleMapping(position).group

    override fun getGroupItem(position: Int): Station {
        val mapping = getVisibleMapping(position)
        return when (mapping) {
            is GroupMapping.Title -> throw IllegalStateException("Should call getGroupTitle()")
            is GroupMapping.Item -> stationList[mapping.index]
        }
    }

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

    override fun isGroupVisible(group: String): Boolean {
        return mappings.find { it.group == group && it is GroupMapping.Item }?.visible == true
    }

    override fun overallSize(): Int = mappings.count { it.visible }

    override fun itemSize(): Int = mappings.count { it.visible && it is GroupMapping.Item }

    override fun getPrevious(item: Station): Station? {
        var prev: GroupMapping.Item? = null
        for (i in 0 until mappings.size) {
            val mapping = getMapping(i)
            if (mapping is GroupMapping.Item) {
                if (mapping.id == item.id && prev != null) {
                    return stationList[prev.index]
                } else if (mapping.visible) {
                    prev = mapping
                }
            }
        }
        return prev?.let { stationList[it.index] }
    }

    override fun getNext(item: Station): Station? {
        var next: GroupMapping.Item? = null
        for (i in mappings.size - 1 downTo 0) {
            val mapping = getMapping(i)
            if (mapping is GroupMapping.Item) {
                if (mapping.id == item.id && next != null) {
                    return stationList[next.index]
                } else if (mapping.visible) {
                    next = mapping
                }
            }
        }
        return next?.let { stationList[it.index] }
    }

    override fun observe(): Observable<GroupedList<Station>> = obs

    private fun notifyObservers() {
        obs.accept(this)
    }

    //region =============== MutableList ==============

    override fun addAll(elements: Collection<Station>): Boolean {
        return stationList.addAll(elements).also {
            initMappings()
            notifyObservers()
        }
    }

    override fun add(element: Station): Boolean {
        return stationList.add(element).also {
            initMappings()
            notifyObservers()
        }
    }

    override fun set(index: Int, element: Station): Station {
        return stationList.set(index, element).also {
            initMappings()
            notifyObservers()
        }
    }

    override fun remove(element: Station): Boolean {
        val removed = stationList.remove(element)
        if (removed) {
            initMappings()
            notifyObservers()
        }
        return removed
    }

    //endregion

    private fun getMapping(position: Int): GroupMapping = mappings[position]

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
}
