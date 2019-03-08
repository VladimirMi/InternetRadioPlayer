package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 07.03.2019.
 */

class RecordsQueue(private val records: List<Record>) : List<Record> by records, MediaQueue {

    override val queueSize = size

    override fun getNext(id: String): Media {
        val currIndex = records.indexOfFirst { it.id == id }
        if (currIndex == -1) throw IllegalStateException("Can't find record with id $id")
        return records[(currIndex + 1) % records.size]
    }

    override fun getPrevious(id: String): Media {
        val currIndex = records.indexOfFirst { it.id == id }
        if (currIndex == -1) throw IllegalStateException("Can't find record with id $id")
        return records[(records.size + currIndex - 1) % records.size]
    }

}