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
}

data class GroupMapping(val group: String, val index: Int? = null) {
    val isGroupTitle get() = index == null
}