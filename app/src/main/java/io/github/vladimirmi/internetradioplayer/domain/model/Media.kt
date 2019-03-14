package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 02.02.2019.
 */

interface Media {

    val id: String
    val name: String
    val uri: String

    companion object {
        fun nullObj() = object : Media {
            override val id = ""
            override val name = ""
            override val uri = ""
        }
    }

    fun isNull() = id.isEmpty()
}