package io.github.vladimirmi.internetradioplayer.model.db.entity

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.getRandomDarkColor
import io.github.vladimirmi.internetradioplayer.extensions.getRandomLightColor
import java.util.*

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

data class Icon(val res: Int,
                val bg: Int,
                val fg: Int) {

    companion object {
        fun randomIcon(): Icon {
            val random = Random()
            return Icon(res = random.nextInt(ICONS.size),
                    fg = getRandomDarkColor(random),
                    bg = getRandomLightColor(random))
        }
    }
}

val ICONS = arrayOf(
        R.drawable.ic_station_1,
        R.drawable.ic_station_2,
        R.drawable.ic_station_3,
        R.drawable.ic_station_4,
        R.drawable.ic_station_5,
        R.drawable.ic_station_6
)
