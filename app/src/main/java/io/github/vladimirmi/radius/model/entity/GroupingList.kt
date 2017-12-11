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

    init {
        initMappings()
    }

    private fun initMappings() {
        mappings.clear()
        stationList.forEachIndexed { index, station ->
            if ((mappings.isEmpty() || mappings.last().group != station.group) && station.group.isNotBlank()) {
                mappings.add(Title(station.group))
            }
            mappings.add(Item(station.group, station.id, index))
        }
    }

    override fun isGroupTitle(position: Int): Boolean = mappings[position] is Title

    override fun getGroupTitle(position: Int): String = mappings[position].group

    override fun getGroupItem(position: Int): Station {
        val mapping = mappings[position]
        return when (mapping) {
            is Title -> throw IllegalStateException("Should call getGroupTitle()")
            is Item -> stationList[mapping.index]
        }
    }

    override fun hideGroup(group: String) {
        mappings.removeAll { it.group == group && it !is Title }
    }

    override fun showGroup(group: String) {
        mappings.addAll(stationList.mapIndexedNotNull { index, station ->
            if (station.group == group) Item(station.group, station.id, index) else null
        })
        sortMappings()
    }

    override fun isGroupVisible(group: String): Boolean =
            mappings.find { it.group == group && it !is Title } != null

    override fun groupedSize(): Int = mappings.size

    override fun getItemPosition(item: Station): Int {
        return mappings.indexOfFirst { it is Item && it.id == item.id }
    }

    override fun getPrevious(item: Station): Station? {
        val items = (0 until groupedSize())
                .map { mappings[it] }
                .filterIsInstance<Item>()
        val visible = isGroupVisible(item.group)
        val skip = items.firstOrNull()?.id == item.id
        return items
                .takeWhile { skip || (if (visible) it.id != item.id else it.group != item.group) }
                .lastOrNull()
                ?.let { stationList[it.index] }
    }

    override fun getNext(item: Station): Station? {
        val reverseItems = (groupedSize() - 1 downTo 0)
                .map { mappings[it] }
                .filterIsInstance<Item>()
        val visible = isGroupVisible(item.group)
        val skip = reverseItems.firstOrNull()?.id == item.id

        return reverseItems
                .takeWhile { skip || (if (visible) it.id != item.id else it.group != item.group) }
                .lastOrNull()
                ?.let { stationList[it.index] }
    }

    override fun observe(): Observable<GroupedList<Station>> = obs

    override fun notifyObservers() {
        obs.accept(this)
    }

    //region =============== MutableList ==============

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

    //endregion

    private fun sortMappings() {
        mappings.sortBy { it.group }
    }
}
