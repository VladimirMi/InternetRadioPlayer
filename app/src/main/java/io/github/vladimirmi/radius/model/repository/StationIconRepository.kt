package io.github.vladimirmi.radius.model.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.source.StationIconSource
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 24.12.2017.
 */

class StationIconRepository
@Inject constructor(private val iconSource: StationIconSource) {

    val currentIcon: BehaviorRelay<Icon> = BehaviorRelay.createDefault(iconSource.defaultIcon)

    fun getStationIcon(path: String): Single<Icon> {
        return Single.fromCallable {
            iconSource.getIcon(path).also {
                setCurrentIcon(it)
            }
        }
    }

    fun getSavedIcon(name: String): Single<Icon> {
        return Single.fromCallable { iconSource.getSavedIcon(name) }
    }

    fun setCurrentIcon(icon: Icon) {
        iconSource.cacheIcon(icon)
        currentIcon.accept(icon)
    }

    fun saveStationIcon(icon: Icon): Completable {
        return Completable.fromCallable {
            iconSource.saveIcon(icon)
            setCurrentIcon(icon)
        }
    }

    fun removeStationIcon(name: String): Completable {
        return Completable.fromCallable { iconSource.removeIcon(name) }
    }
}