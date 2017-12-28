package io.github.vladimirmi.radius.model.interactor

import android.net.Uri
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Station
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
                    private val iconInteractor: IconInteractor) {

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
        return if (stationList.find { it.title == station.title } == null) {
            stationRepository.addStation(station)
                    .mergeWith(iconInteractor.saveCurrentIcon(station.title))
                    .doOnComplete { currentStation = station }
                    .toSingle { true }
        } else Single.just(false)
    }

    fun updateCurrentStation(newStation: Station): Completable {
        Timber.e("updateCurrentStation: $currentStation")
        Timber.e("updateCurrentStation: new $newStation")
        val updateStation = if (newStation != currentStation) {
            stationRepository.updateStation(newStation)
        } else Completable.complete()

        val updateIcon = iconInteractor.saveCurrentIcon(newStation.title)

        val remove = if (newStation.title != currentStation.title) {
            removeStation(currentStation)
        } else Completable.complete()

        return updateStation.mergeWith(updateIcon)
                .concatWith(remove)
                .doOnComplete { currentStation = newStation }
    }

    fun removeStation(station: Station): Completable {
        return stationRepository.removeStation(station)
                .mergeWith(iconInteractor.removeIcon(station.title))
    }

    fun showOrHideGroup(group: String) {
        if (stationList.isGroupVisible(group)) {
            stationRepository.hideGroup(group)
        } else {
            stationRepository.showGroup(group)
        }
    }
}