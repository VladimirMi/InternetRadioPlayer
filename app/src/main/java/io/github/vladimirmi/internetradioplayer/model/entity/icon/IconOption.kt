package io.github.vladimirmi.internetradioplayer.model.entity.icon

import io.github.vladimirmi.internetradioplayer.R
import java.util.*

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
                ICON
            }
        }
    }
}

enum class IconResource(val id: Int, val resId: Int) {
    ICON_1(R.id.ic_station_1, R.drawable.ic_station_1),
    ICON_2(R.id.ic_station_2, R.drawable.ic_station_2),
    ICON_3(R.id.ic_station_3, R.drawable.ic_station_3),
    ICON_4(R.id.ic_station_4, R.drawable.ic_station_4);

    companion object {
        private val MAP: Map<Int, IconResource> by lazy {
            IconResource.values().associateBy(IconResource::id)
        }

        fun fromId(id: Int): IconResource = MAP[id]!!

        fun fromName(name: String): IconResource {
            return try {
                IconResource.valueOf(name)
            } catch (e: IllegalArgumentException) {
                ICON_1
            }
        }

        fun random(): IconResource {
            val idx = Random().nextInt(IconResource.values().size)
            return IconResource.values()[idx]
        }
    }
}
