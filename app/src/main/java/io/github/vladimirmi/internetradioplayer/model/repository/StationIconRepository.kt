package io.github.vladimirmi.internetradioplayer.model.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.model.entity.icon.Icon
import io.github.vladimirmi.internetradioplayer.model.source.StationIconSource
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 24.12.2017.
 */

class StationIconRepository
@Inject constructor(private val iconSource: StationIconSource) {

    val currentIcon: BehaviorRelay<Icon> = BehaviorRelay.create()

    fun getStationIcon(path: String): Single<Icon> {
        return Single.fromCallable { iconSource.getIcon(path) }
    }

    fun getSavedIcon(name: String): Single<Icon> {
        return Single.fromCallable { iconSource.getSavedIcon(name) }
    }

    fun saveStationIcon(newName: String): Completable {
        return Completable.fromCallable {
            val icon = currentIcon.value
            iconSource.removeFromCache(icon.name)
            icon.name = newName
            iconSource.cache(icon)

            iconSource.saveIcon(icon)
            currentIcon.accept(icon)
        }
    }

    fun removeStationIcon(name: String): Completable {
        return Completable.fromCallable { iconSource.removeIcon(name) }
    }
}
