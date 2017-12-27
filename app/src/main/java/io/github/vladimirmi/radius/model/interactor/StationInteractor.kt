package io.github.vladimirmi.radius.model.interactor

import android.net.Uri
import io.github.vladimirmi.radius.extensions.toSingle
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.StationListRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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
                .doOnNext { iconInteractor.setCurrentIcon(it.title) }
    }

    var currentStation: Station
        get() = stationRepository.currentStation.value
        set(value) = stationRepository.setCurrentStation(value)

    fun createStation(uri: Uri): Completable {
        return { stationRepository.createStation(uri) }.toSingle()
                .subscribeOn(Schedulers.io())
                .doOnSuccess {
                    previousWhenCreate = currentStation
                    stationRepository.currentStation.accept(it)
//                    playerModeObs.accept(PlayerMode.NEXT_PREVIOUS_DISABLED)
                }.toCompletable()
    }

    fun addStation(station: Station): Single<Boolean> {
        return Single.fromCallable {
            if (stationList.find { it.title == station.title } == null) {
                stationRepository.addStation(station)
                //        currentStation = station
                iconInteractor.saveIcon(station.title).blockingAwait()
                true
            } else false
        }
    }

    fun updateCurrentStation(newStation: Station): Completable {
        return currentStationObs().firstOrError()
                .flatMapCompletable { station ->
                    Completable.fromCallable {
                        if (newStation.title != station.title) {
                            removeStation(station).blockingAwait()
                        }
                        if (newStation != station) {
                            stationRepository.updateStation(newStation)
                            iconInteractor.saveIcon(station.title).blockingAwait()
                            currentStation = newStation
                        }
                    }
                }
    }

    fun removeStation(station: Station): Completable {
        return Completable.fromCallable {
            stationRepository.removeStation(station)
            iconInteractor.removeIcon(station.title)
        }
    }

    fun showOrHideGroup(group: String) {
        if (stationList.isGroupVisible(group)) {
            stationRepository.hideGroup(group)
        } else {
            stationRepository.showGroup(group)
        }
    }
}