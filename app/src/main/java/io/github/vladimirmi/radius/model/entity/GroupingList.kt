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

    override fun isGroupTitle(position: Int): Boolean = getGroupMapping(position).isGroupTitle

    override fun getGroupTitle(position: Int): String = getGroupMapping(position).group

    override fun getGroupItem(position: Int): Station {
        val groupMapping = getGroupMapping(position)
        if (groupMapping.index == null) throw IllegalStateException("Should call getGroupTitle()")
        val group = groups[groupMapping.group] ?: throw IllegalStateException("Can not find group")
        return get(group[groupMapping.index])
    }

    override fun hideGroup(group: String) {
        setGroupVisible(group, false)
    }

    override fun showGroup(group: String) {
        setGroupVisible(group, true)
    }

    override fun isGroupVisible(group: String): Boolean =
            mappings.find { it.group == group && !it.isGroupTitle }?.visible ?: false

    override fun groupedSize(): Int = mappings.count { it.visible }

    private fun initMappings() {
        stationList.forEachIndexed { index, media ->
            addToMappings(media, index)
        }
        sortMappings()
    }

    private fun addToMappings(station: Station, index: Int) {
        val group = station.group
        val list = groups.getOrPut(group) { ArrayList() }
        if (list.isEmpty()) mappings.add(GroupMapping(group))
        mappings.add(GroupMapping(group, list.size))
        list.add(index)
    }

    private fun sortMappings() {
        mappings.sortBy { it.group }
    }

    private fun getGroupMapping(position: Int): GroupMapping {
        val hided = (0..position).count { !mappings[it].visible }
        return mappings[position + hided]
    }

    private fun setGroupVisible(group: String, visible: Boolean) {
        mappings.forEach {
            if (it.group == group && !it.isGroupTitle) {
                it.visible = visible
            }
        }
    }

    private val obs: BehaviorRelay<GroupedList<Station>> = BehaviorRelay.createDefault(this)

    override fun observe(): Observable<GroupedList<Station>> = obs

    override fun notifyObservers() {
        obs.accept(this)
    }

    override fun add(element: Station): Boolean {
        addToMappings(element, stationList.size)
        sortMappings()
        val tru = stationList.add(element)
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
            groups.clear()
            mappings.clear()
            initMappings()
            obs.accept(this)
        }
        return removed
    }
}
