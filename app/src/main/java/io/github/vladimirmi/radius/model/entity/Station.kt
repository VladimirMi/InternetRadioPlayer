package io.github.vladimirmi.radius.model.entity

import android.net.Uri

/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

data class Station(val uri: Uri,
                   val path: String,
                   val fav: Boolean = false) {

    val group = path.substringBeforeLast("/").substringAfterLast("/")
    val title = path.substringAfterLast("/")

    companion object {

    }
}

