package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

/**
 * Created by Vladimir Mikhalev 02.11.2017.
 */
interface GroupedList<E> {

    val size: Int

    val itemsSize: Int

    val overallSize: Int

    fun isGroupTitle(position: Int): Boolean

    fun getGroupTitle(position: Int): String

    fun getGroupItem(position: Int): E

    fun getGroupItem(id: String): E?

    fun isGroupExpanded(group: String): Boolean

    fun getPrevious(element: E): E?

    fun getNext(element: E): E?

    fun positionOfFirst(id: String): Int

    fun contains(predicate: (E) -> Boolean): Boolean
}


