package io.github.vladimirmi.radius.model.entity.GroupedList

import io.github.vladimirmi.radius.model.entity.Filter
import io.github.vladimirmi.radius.model.entity.Station
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 02.11.2017.
 */
interface GroupedList<E> {

    fun isGroupTitle(position: Int): Boolean

    fun getGroupTitle(position: Int): String

    fun getGroupItem(position: Int): E

    operator fun get(position: Int): E

    fun isGroupVisible(group: String): Boolean

    fun observe(): Observable<GroupedList<E>>

    fun getPrevious(element: E): E?

    fun getNext(element: E): E?

    val size: Int

    val itemSize: Int

    val overallSize: Int

    val filter: Filter

    fun contains(element: Station): Boolean

    fun firstOrNull(): Station

    fun indexOf(station: E): Int

    fun indexOfFirst(predicate: (E) -> Boolean): Int

    fun hasItems(predicate: (E) -> Boolean): Boolean

    fun canFilter(filter: Filter): Boolean
}


