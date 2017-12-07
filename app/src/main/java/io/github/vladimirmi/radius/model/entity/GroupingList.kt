package io.github.vladimirmi.radius.model.entity

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import java.lang.IllegalStateException

/**
 * Created by Vladimir Mikhalev 11.10.2017.
 */

class GroupingList(private val stationList: ArrayList<Station>)
    : MutableList<Station> by stationList, GroupedList<Station> {

    private val groups = HashMap<String, ArrayList<Int>>()
    private val mappings = ArrayList<GroupMapping>()

    init {
        initMappings()
    }

    private fun initMappings() {
        groups.clear()
        mappings.clear()
        stationList.forEachIndexed { index, station ->
            val list = groups.getOrPut(station.group) { ArrayList() }
            if (list.isEmpty() && station.group.isNotBlank()) {
                mappings.add(GroupMapping(station.group))
            }
            mappings.add(GroupMapping(station.group, list.size, station.id))
            list.add(index)
        }
        sortMappings()
    }

    private fun sortMappings() {
        mappings.sortBy { it.group }
    }

    override fun isGroupTitle(position: Int): Boolean = mappings[position].isGroupTitle

    override fun getGroupTitle(position: Int): String = mappings[position].group

    override fun getGroupItem(position: Int): Station {
        val groupMapping = mappings[position]
        if (groupMapping.index == null) throw IllegalStateException("Should call getGroupTitle()")
        val group = groups[groupMapping.group] ?: throw IllegalStateException("Can not find group")
        return get(group[groupMapping.index])
    }

    override fun hideGroup(group: String) {
        mappings.removeAll { !it.isGroupTitle && it.group == group }
    }

    override fun showGroup(group: String) {
        mappings.addAll(groups[group]!!.mapIndexed { index, _ -> GroupMapping(group, index) })
        sortMappings()
    }

    override fun isGroupVisible(group: String): Boolean =
            mappings.find { it.group == group && !it.isGroupTitle } != null

    override fun groupedSize(): Int = mappings.size

    override fun getPosition(station: Station): Int = mappings.indexOfFirst { it.id == station.id }

    private val obs: BehaviorRelay<GroupedList<Station>> = BehaviorRelay.createDefault(this)

    override fun observe(): Observable<GroupedList<Station>> = obs

    override fun notifyObservers() {
        obs.accept(this)
    }

    override fun add(element: Station): Boolean {
        val tru = stationList.add(element)
        initMappings()
        obs.accept(this)
        return tru
    }

    override fun set(index: Int, element: Station): Station {
        val old = stationList.set(index, element)
        obs.accept(this)
        return old
    }

    override fun remove(element: Station): Boolean {
        val removed = stationList.remove(element)
        if (removed) {
            initMappings()
            obs.accept(this)
        }
        return removed
    }
}
