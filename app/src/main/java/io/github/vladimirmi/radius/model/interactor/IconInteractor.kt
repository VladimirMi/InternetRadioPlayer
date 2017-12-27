package io.github.vladimirmi.radius.model.interactor

import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.repository.StationIconRepository
import io.github.vladimirmi.radius.model.repository.StationListRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 20.12.2017.
 */

class IconInteractor
@Inject constructor(private val iconRepository: StationIconRepository,
                    private val stationListRepository: StationListRepository) {


    fun getIcon(path: String): Single<Icon> {
        return Single.fromCallable { iconRepository.getStationIcon(path) }
    }

    fun getCurrentIcon(): Observable<Icon> {
        return stationListRepository.currentStation
                .map { iconRepository.getStationIcon(it.title).copy(text = it.title.first().toString()) }
    }

    fun setCurrentIcon(name: String) {
        iconRepository.setCurrentIcon(iconRepository.getStationIcon(name))
    }

    fun cacheIcon(icon: Icon): Completable {
        return Completable.fromCallable { iconRepository.setCurrentIcon(icon) }
    }

    fun removeIcon(name: String): Completable {
        return Completable.fromCallable { iconRepository.removeStationIcon(name) }
    }

    fun saveIcon(name: String): Completable {
        Timber.e("saveCurrentIcon: ")
        return getIcon(name).flatMapCompletable { icon ->
            Completable.fromCallable {
                val savedIcon = iconRepository.getSavedIcon(icon.name)
                if (icon != savedIcon) iconRepository.saveStationIcon(icon)
            }
        }
    }
}