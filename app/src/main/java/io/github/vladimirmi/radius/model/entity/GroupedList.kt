package io.github.vladimirmi.radius.model.entity

/**
 * Created by Vladimir Mikhalev 02.11.2017.
 */
interface GroupedList<out E> {
    fun isGroupTitle(position: Int): Boolean
    fun getGroupTitle(position: Int): String
    fun getGroupItem(position: Int): E
    fun isGroupVisible(group: String): Boolean
    fun hideGroup(group: String)
    fun showGroup(group: String)
    fun groupedSize(): Int
}

class GroupMapping(val group: String, val index: Int? = null) {
    var visible = true
    fun isGroupTitle() = index == null
}