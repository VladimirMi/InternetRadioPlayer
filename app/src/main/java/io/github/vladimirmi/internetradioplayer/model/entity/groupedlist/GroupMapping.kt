package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

/**
 * Created by Vladimir Mikhalev 08.01.2018.
 */

sealed class GroupMapping(val group: String, var visible: Boolean) {

    class Title(group: String, visible: Boolean = true) : GroupMapping(group, visible) {
        override fun toString(): String = "Title($group)"
    }

    class Item(group: String, val id: String, val index: Int, visible: Boolean = true)
        : GroupMapping(group, visible) {

        override fun toString(): String = "Item(index=$index)"
    }
}