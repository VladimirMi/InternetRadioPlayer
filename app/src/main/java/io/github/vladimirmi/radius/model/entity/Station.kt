package io.github.vladimirmi.radius.model.entity

import java.util.*


/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

data class Station(val uri: String,
                   val title: String,
                   val group: String = "",
                   val genre: List<String> = emptyList(),
                   val url: String = "",
                   val bitrate: Int = 0,
                   val sample: Int = 0,
                   val favorite: Boolean = false,
                   val id: String = UUID.randomUUID().toString()) {

    companion object {
        fun nullObject() = Station("", "")

        fun Station.isNull() = uri.isEmpty() && title.isEmpty()
    }

}



