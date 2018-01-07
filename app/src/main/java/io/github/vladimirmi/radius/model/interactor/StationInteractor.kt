package io.github.vladimirmi.radius.model.interactor

import android.net.Uri
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Icon
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.StationIconRepository
import io.github.vladimirmi.radius.model.repository.StationListRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class StationInteractor
@Inject constructor(private val stationRepository: StationListRepository,
                    private val iconRepository: StationIconRepository) {

    private val stationList get() = stationRepository.stationList

    private var previousWhenCreate: Station? = null

    var isCreateMode: Boolean
        get() = previousWhenCreate != null
        set(value) {
            if (!value) previousWhenCreate = null
        }

    fun hasStations(): Boolean {
        return stationList.isNotEmpty()
    }

    fun stationListObs(): Observable<GroupedList<Station>> {
        return stationList.observe()
    }

    fun currentStationObs(): Observable<Station> {
        return stationRepository.currentStation
                .flatMapSingle { station ->
                    getIcon(station.name)
                            .doOnSuccess { currentIcon = it }
                            .map { station }
                }
    }

    var currentStation: Station
        get() = stationRepository.currentStation.value
        set(value) = stationRepository.setCurrentStation(value)

    fun createStation(uri: Uri): Single<Station> {
        return stationRepository.createStation(uri)
                .doOnSuccess {
                    previousWhenCreate = currentStation
                    stationRepository.currentStation.accept(it)
//                    playerModeObs.accept(PlayerMode.NEXT_PREVIOUS_DISABLED)
                }
    }

    fun addStation(station: Station): Single<Boolean> {
        return if (stationList.find { it.name == station.name } == null) {
            stationRepository.addStation(station)
                    .mergeWith(saveCurrentIcon(station.name))
                    .doOnComplete { currentStation = station }
                    .toSingle { true }
        } else Single.just(false)
    }

    fun updateCurrentStation(newStation: Station): Completable {
        val updateStation = if (newStation != currentStation) {
            stationRepository.updateStation(newStation)
        } else Completable.complete()

        val updateIcon = saveCurrentIcon(newStation.name)

        val remove = if (newStation.name != currentStation.name) {
            removeStation(currentStation)
        } else Completable.complete()

        return updateStation.mergeWith(updateIcon)
                .concatWith(remove)
                .doOnComplete { currentStation = newStation }
    }

    fun removeStation(station: Station): Completable {
        return stationRepository.removeStation(station)
                .mergeWith(removeIcon(station.name))
    }

    fun showOrHideGroup(group: String) {
        if (stationList.isGroupVisible(group)) {
            stationRepository.hideGroup(group)
        } else {
            stationRepository.showGroup(group)
        }
    }

    //region =============== Icon ==============

    fun iconChanged(): Single<Boolean> =
            iconRepository.getSavedIcon(currentIcon.name)
                    .map { currentIcon != it }

    var currentIcon: Icon
        get() = iconRepository.currentIcon.value
        set(value) = iconRepository.setCurrentIcon(value)

    fun currentIconObs(): Observable<Icon> {
        return iconRepository.currentIcon
    }

    fun getIcon(path: String): Single<Icon> {
        return iconRepository.getStationIcon(path)
    }

    private fun removeIcon(name: String): Completable {
        return iconRepository.removeStationIcon(name)
    }

    private fun saveCurrentIcon(newName: String): Completable {
        val newIcon = currentIcon.copy(name = newName)
        return iconChanged()
                .flatMapCompletable { changed ->
                    if (changed || currentIcon.name != newName) {
                        Timber.e("saveCurrentIcon: ")
                        iconRepository.saveStationIcon(newIcon)
                    } else {
                        Completable.complete()
                    }
                }
    }

    //endregion
}