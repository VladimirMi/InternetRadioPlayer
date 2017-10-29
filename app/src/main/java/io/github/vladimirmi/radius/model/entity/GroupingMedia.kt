package io.github.vladimirmi.radius.model.entity

import java.lang.IllegalStateException
import java.util.NoSuchElementException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by Vladimir Mikhalev 11.10.2017.
 */

class GroupingMedia {

    class GroupMapping(val group: String, val index: Int? = null) {
        fun isGroupTitle() = index == null
    }

    private val groups = HashMap<String, ArrayList<Media>>()
    private val idxMapping = ArrayList<GroupMapping>()

    fun setData(mediaList: List<Media>) {
        for (media in mediaList) {
            val key = media.genres.first()
            val list = groups.getOrPut(key) { ArrayList() }
            if (list.isEmpty()) idxMapping.add(GroupMapping(key))
            idxMapping.add(GroupMapping(key, list.size))
            list.add(media)
        }
    }

    fun isGroupTitle(position: Int): Boolean {
        checkRange(position)
        return idxMapping[position].isGroupTitle()
    }

    fun getGroupTitle(position: Int): String {
        checkRange(position)
        return idxMapping[position].group
    }

    fun getGroupItem(position: Int): Media {
        checkRange(position)
        val groupMapping = idxMapping[position]
        if (groupMapping.index == null) throw IllegalStateException("Should call getGroupTitle()")
        return groups[groupMapping.group]!![groupMapping.index]
    }

    fun size() = idxMapping.size

    private fun checkRange(position: Int) {
        if (position < 0 || position >= idxMapping.size) {
            throw  IndexOutOfBoundsException()
        }
    }

    fun getGroupItemPosition(media: Media): Int {
        idxMapping.forEachIndexed { groupIndex, groupMapping ->
            val mediaList = groups[groupMapping.group]!!
            mediaList.forEachIndexed { mediaIndex, m ->
                if (m == media) return groupIndex + mediaIndex + 1
            }
        }
        throw NoSuchElementException()
    }
}
