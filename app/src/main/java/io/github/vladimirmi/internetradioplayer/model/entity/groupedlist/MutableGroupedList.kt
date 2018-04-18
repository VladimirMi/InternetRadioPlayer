package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

import io.github.vladimirmi.internetradioplayer.model.entity.Filter
import io.github.vladimirmi.internetradioplayer.model.entity.Station

/**
 * Created by Vladimir Mikhalev 08.01.2018.
 */

interface MutableGroupedList<E> : GroupedList<E> {

    fun hideGroup(group: String)

    fun showGroup(group: String)

    fun addAll(elements: Collection<Station>): Boolean

    fun add(element: Station): Boolean

    operator fun set(index: Int, element: Station): Station

    fun remove(element: Station): Boolean

    fun filter(filter: Filter)
}
