package io.github.vladimirmi.radius.model.entity

import io.reactivex.Observable

/**
 * Created by Vladimir Mikhalev 02.11.2017.
 */
interface GroupedList<E> {

    fun isGroupTitle(position: Int): Boolean

    fun getGroupTitle(position: Int): String

    fun getGroupItem(position: Int): E

    fun isGroupVisible(group: String): Boolean

    fun hideGroup(group: String)

    fun showGroup(group: String)

    fun groupedSize(): Int

    fun observe(): Observable<GroupedList<E>>

    fun notifyObservers()

    fun getItemPosition(item: E): Int

    fun getPrevious(item: E): E?

    fun getNext(item: E): E?

}


sealed class GroupMapping(val group: String)
class Title(group: String) : GroupMapping(group) {
    override fun toString(): String = "Title($group)"
}

class Item(group: String, val id: String, val index: Int) : GroupMapping(group) {
    override fun toString(): String = "Item(index=$index)"
}