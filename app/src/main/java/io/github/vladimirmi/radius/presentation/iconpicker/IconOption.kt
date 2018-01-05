package io.github.vladimirmi.radius.presentation.iconpicker

import io.github.vladimirmi.radius.R

/**
 * Created by Vladimir Mikhalev 27.12.2017.
 */

enum class IconOption(val id: Int) {

    ICON(R.id.optionIconBt),
    FAVICON(R.id.optionFaviconBt),
    TEXT(R.id.optionTextBt),
    CUSTOM(R.id.optionAddBt);

    companion object {
        private val MAP: Map<Int, IconOption> by lazy {
            IconOption.values().associateBy(IconOption::id)
        }

        fun fromId(id: Int): IconOption = MAP[id]!!

        fun fromName(name: String): IconOption {
            return try {
                IconOption.valueOf(name)
            } catch (e: IllegalArgumentException) {
                IconOption.ICON
            }
        }
    }
}

enum class IconRes(val id: Int, val resId: Int) {
    ICON_1(R.id.ic_station_1, R.drawable.ic_station_1),
    ICON_2(R.id.ic_station_2, R.drawable.ic_station_2),
    ICON_3(R.id.ic_station_3, R.drawable.ic_station_3),
    ICON_4(R.id.ic_station_4, R.drawable.ic_station_4);

    companion object {
        private val MAP: Map<Int, IconRes> by lazy {
            IconRes.values().associateBy(IconRes::id)
        }

        fun fromId(id: Int): IconRes = MAP[id]!!

        fun fromName(name: String): IconRes {
            return try {
                IconRes.valueOf(name)
            } catch (e: IllegalArgumentException) {
                IconRes.ICON_1
            }
        }
    }
}