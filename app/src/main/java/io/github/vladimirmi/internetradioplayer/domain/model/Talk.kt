package io.github.vladimirmi.internetradioplayer.domain.model

import io.github.vladimirmi.internetradioplayer.data.db.entity.Group

/**
 * Created by Vladimir Mikhalev 24.03.2019.
 */

data class Talk(
        override val id: String,
        override val name: String,
        override val uri: String,
        override val remoteId: String,
        override val group: String = Group.DEFAULT_NAME,
        override val specs: String? = null,
        override val description: String? = null,
        override val genre: String? = null,
        override val language: String? = null,
        override val location: String? = null,
        override val website: String? = null,
        val timeleft: Int = 0,
        val timeplayed: Int = 0
) : Media {

    val isLive = timeleft > 0
}