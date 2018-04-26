package io.github.vladimirmi.internetradioplayer.model.interactor

import android.net.Uri
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.ValidationException
import io.github.vladimirmi.internetradioplayer.model.entity.Filter
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.model.entity.groupedlist.GroupedList
import io.github.vladimirmi.internetradioplayer.model.entity.icon.Icon
import io.github.vladimirmi.internetradioplayer.model.manager.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.model.repository.StationIconRepository
import io.github.vladimirmi.internetradioplayer.model.repository.StationListRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class StationInteractor
@Inject constructor(private val stationRepository: StationListRepository,
                    private val iconRepository: StationIconRepository,
                    private val shortcutHelper: ShortcutHelper) {

    var previousWhenCreate: Station? = null
        private set

    var isCreateMode: Boolean
        get() = previousWhenCreate != null
        set(value) {
            if (!value) previousWhenCreate = null
        }

    val stationList: GroupedList<Station> get() = stationRepository.stationList

    val stationListObs: Observable<GroupedList<Station>> get() = stationList.observe()

    var currentStation: Station
        get() = stationRepository.currentStation.value
        set(value) = stationRepository.setCurrentStation(value)

    val currentStationObs: Observable<Station>
        get() = stationRepository.currentStation
                .flatMapSingle { station ->
                    getIcon(station.name)
                            .doOnSuccess { currentIcon = it }
                            .map { station }
                }

    fun initStations(): Completable {
        return Completable.fromCallable(stationRepository::initStations)
    }

    fun getStation(id: String): Station? {
        return stationList.firstOrNull { it.id == id }
    }

    fun haveStations(): Boolean {
        return stationRepository.isInitialized && stationList.haveItems()
    }

    fun createStation(uri: Uri): Single<Station> {
        return stationRepository.createStation(uri)
                .doOnSuccess {
                    previousWhenCreate = currentStation
                    stationRepository.currentStation.accept(it)
                }
    }

    fun addStation(station: Station): Completable {
        return validate(station, adding = true).andThen(
                stationRepository.addStation(station)
                        .mergeWith(saveCurrentIcon(station.name)))
                .doOnComplete { currentStation = station }
    }

    private fun validate(station: Station, adding: Boolean = false): Completable {
        return when {
            stationList.haveItems { it.name == station.name } && adding -> {
                Completable.error(ValidationException(R.string.toast_name_exists_error))
            }
            station.name.isBlank() -> {
                Completable.error(ValidationException(R.string.toast_name_empty_error))
            }
            else -> Completable.complete()
        }
    }

    fun updateCurrentStation(newStation: Station): Completable {
        val currentPosition = stationList.positionOfFirst { it.id == currentStation.id }

        val updateStation = if (newStation != currentStation) {
            stationRepository.updateStation(newStation)
        } else Completable.complete()

        val remove = if (newStation.name != currentStation.name) {
            removeStation(currentStation)
        } else Completable.complete()

        return validate(newStation)
                .andThen(saveCurrentIcon(newStation.name)
                        .mergeWith(updateStation)
                        .concatWith(remove))
                .doOnComplete {
                    currentStation = if (stationList.contains(newStation)) {
                        newStation
                    } else {
                        val newPos = (stationList.itemsSize + currentPosition - 1) % stationList.itemsSize
                        stationList.getGroupItem(newPos)
                    }
                }
    }

    fun nextStation(cycle: Boolean = true): Boolean {
        val next = stationRepository.stationList.getNext(stationRepository.currentStation.value, cycle)
        return if (next != null) {
            stationRepository.setCurrentStation(next)
            true
        } else false
    }

    fun previousStation(cycle: Boolean = true): Boolean {
        val previous = stationRepository.stationList.getPrevious(stationRepository.currentStation.value, cycle)
        return if (previous != null) {
            stationRepository.setCurrentStation(previous)
            true
        } else false
    }

    fun removeStation(station: Station): Completable {
        return stationRepository.removeStation(station)
                .mergeWith(removeIcon(station.name))
                .doOnComplete { shortcutHelper.removeShortcut(station) }
    }

    fun showOrHideGroup(group: String) {
        if (stationList.isGroupVisible(group)) {
            stationRepository.hideGroup(group)
        } else {
            stationRepository.showGroup(group)
        }
    }

    fun filterStations(filter: Filter) {
        stationRepository.filterStations(filter)
    }

    fun addCurrentShortcut(): Boolean {
        return shortcutHelper.pinShortcut(currentStation, currentIcon)
    }

    //region =============== Icon ==============

    fun iconChanged(): Single<Boolean> =
            iconRepository.getSavedIcon(currentIcon.name)
                    .map { currentIcon != it }

    var currentIcon: Icon
        get() = iconRepository.currentIcon.value
        set(value) = iconRepository.currentIcon.accept(value)

    val currentIconObs: Observable<Icon> get() = iconRepository.currentIcon

    fun getIcon(path: String): Single<Icon> = iconRepository.getStationIcon(path)

    private fun removeIcon(name: String): Completable {
        return iconRepository.removeStationIcon(name)
    }

    private fun saveCurrentIcon(newName: String): Completable {
        return iconChanged().flatMapCompletable { changed ->
            if (changed || currentIcon.name != newName) {
                iconRepository.saveStationIcon(newName)
            } else {
                Completable.complete()
            }
        }
    }

    //endregion
}
