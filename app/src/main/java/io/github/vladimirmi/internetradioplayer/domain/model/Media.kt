package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

interface Media {

    val id: String
    val name: String
    val uri: String
    val remoteId: String

    companion object {
        fun nullObj() = object : Media {
            override val id = ""
            override val name = ""
            override val uri = ""
            override val remoteId = ""
        }
    }

    fun isNull() = id.isEmpty()
}