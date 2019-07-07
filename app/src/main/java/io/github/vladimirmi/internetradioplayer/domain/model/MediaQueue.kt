package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 07.03.2019.
 */

interface MediaQueue {

    //todo refactor: return not null
    fun getNext(id: String): Media?

    fun getPrevious(id: String): Media?
}

class SingletonMediaQueue : MediaQueue {

    override fun getNext(id: String): Media? {
        return null
    }

    override fun getPrevious(id: String): Media? {
        return null
    }
}