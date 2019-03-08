package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 07.03.2019.
 */

interface MediaQueue {

    val queueSize: Int

    fun getNext(id: String): Media

    fun getPrevious(id: String): Media
}

class SingletonMediaQueue(private val media: Media) : MediaQueue {
    override val queueSize = 1

    override fun getNext(id: String): Media {
        if (media.id != id) throw IllegalStateException("Can't find station with id $id")
        return media
    }

    override fun getPrevious(id: String): Media {
        if (media.id != id) throw IllegalStateException("Can't find station with id $id")
        return media
    }
}