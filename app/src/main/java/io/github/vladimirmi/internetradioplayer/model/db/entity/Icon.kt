package io.github.vladimirmi.internetradioplayer.model.db.entity

import io.github.vladimirmi.internetradioplayer.R

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

class Icon {

    var res: Int = 0
    var bg: Int = 0
    var fg: Int = 0

    fun copy() = Icon().also { it.res = res; it.bg = bg; it.fg = fg }
}

val ICONS = arrayOf(
        R.drawable.ic_station_1,
        R.drawable.ic_station_2,
        R.drawable.ic_station_3,
        R.drawable.ic_station_4,
        R.drawable.ic_station_5,
        R.drawable.ic_station_6
)
