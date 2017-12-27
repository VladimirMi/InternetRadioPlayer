package io.github.vladimirmi.radius.model.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.source.StationIconSource
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 24.12.2017.
 */

class StationIconRepository
@Inject constructor(private val iconSource: StationIconSource) {

    val currentIcon: BehaviorRelay<Icon> = BehaviorRelay.createDefault(getStationIcon(""))

    fun getStationIcon(path: String): Icon {
        return iconSource.getIcon(path).also {
            setCurrentIcon(it)
        }
    }

    fun getSavedIcon(name: String): Icon {
        return iconSource.getSavedIcon(name)
    }

    fun setCurrentIcon(icon: Icon) {
        iconSource.cacheIcon(icon)
        currentIcon.accept(icon)
    }

    fun saveStationIcon(icon: Icon) {
        iconSource.saveIcon(icon)
        setCurrentIcon(icon)
    }

    fun removeStationIcon(name: String) {
        iconSource.removeIcon(name)
    }
}