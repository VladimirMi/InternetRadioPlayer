package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

import io.github.vladimirmi.internetradioplayer.model.entity.Filter
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 02.11.2017.
 */
interface GroupedList<E> {

    operator fun get(index: Int): E?

    fun isGroupTitle(position: Int): Boolean

    fun getGroupTitle(position: Int): String

    fun getGroupItem(position: Int): E

    fun isGroupVisible(group: String): Boolean

    fun observe(): Observable<GroupedList<E>>

    fun getPrevious(element: E, cycle: Boolean = true): E?

    fun getNext(element: E, cycle: Boolean = true): E?

    val size: Int

    val itemsSize: Int

    val overallSize: Int

    val filter: Filter

    fun contains(element: E): Boolean

    fun indexOfFirst(predicate: (E) -> Boolean): Int

    fun firstOrNull(predicate: (E) -> Boolean = { true }): E?

    fun haveItems(predicate: (E) -> Boolean = { true }): Boolean

    fun canFilter(filter: Filter): Boolean

    fun positionOfFirst(predicate: (Station) -> Boolean): Int
}


