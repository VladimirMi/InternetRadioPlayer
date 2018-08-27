package io.github.vladimirmi.internetradioplayer.model.entity.groupedlist

/**
 * Created by Vladimir Mikhalev 24.08.2018.
 */

class Group<E>(var title: String, var expanded: Boolean, var elements: MutableList<E>, var order: Int) {

    val id = title

    fun size() = elements.size
}
