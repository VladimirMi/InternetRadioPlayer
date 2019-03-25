package io.github.vladimirmi.internetradioplayer.domain.model

/**
 * Created by Vladimir Mikhalev 24.03.2019.
 */

data class Talk(override val id: String,
                override val name: String,
                override val uri: String,
                override val remoteId: String,
                val timeleft: Int,
                val timeplayed: Int) : Media {

    val isLive = timeleft > 0
}