package io.github.vladimirmi.radius.model.interactor

import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.repository.StationIconRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 20.12.2017.
 */

class IconInteractor
@Inject constructor(private val iconRepository: StationIconRepository) {

    var currentIcon: Icon
        get() = iconRepository.currentIcon.value
        set(value) = iconRepository.setCurrentIcon(value)

    fun currentIconObs(): Observable<Icon> {
        return iconRepository.currentIcon
    }

    fun getIcon(path: String): Single<Icon> {
        return iconRepository.getStationIcon(path)
    }

    fun removeIcon(name: String): Completable {
        return iconRepository.removeStationIcon(name)
    }

    fun saveCurrentIcon(newName: String): Completable {
        val newIcon = currentIcon.copy(name = newName)
        return iconRepository.getSavedIcon(currentIcon.name)
                .flatMapCompletable { savedIcon ->
                    Timber.e("saveCurrentIcon: $newIcon")
                    Timber.e("saveCurrentIcon: saved $savedIcon")
//                    if (currentIcon != savedIcon) {
                    iconRepository.saveStationIcon(newIcon)
//                    } else {
//                    }
                }
    }
}