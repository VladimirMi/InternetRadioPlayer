package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

import io.github.vladimirmi.internetradioplayer.model.db.entity.Group
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station

/**
 * Created by Vladimir Mikhalev 02.11.2017.
 */
interface GroupedList {

    val size: Int

    val itemsSize: Int

    val overallSize: Int

    fun isGroup(position: Int): Boolean

    fun getGroup(position: Int): Group

    fun getGroupItem(position: Int): Station

    fun getGroupItemById(id: String): Station?

    fun isGroupExpanded(id: String): Boolean

    fun getPreviousFrom(id: String): Station?

    fun getNextFrom(id: String): Station?

    fun positionOfFirst(id: String): Int

    fun contains(predicate: (Station) -> Boolean): Boolean
}


