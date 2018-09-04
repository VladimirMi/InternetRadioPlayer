package io.github.vladimirmi.internetradioplayer.model.interactor

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.ValidationException
import io.github.vladimirmi.internetradioplayer.model.db.entity.Group
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station
import io.github.vladimirmi.internetradioplayer.model.entity.groupedlist.GroupedList
import io.github.vladimirmi.internetradioplayer.model.entity.groupedlist.StationsGroupList
import io.github.vladimirmi.internetradioplayer.model.manager.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.model.repository.StationIconRepository
import io.github.vladimirmi.internetradioplayer.model.repository.StationListRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
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

    private val stationsList = StationsGroupList()
    private val _stationsListObs = BehaviorRelay.create<GroupedList>()
    val stationsListObs: Observable<GroupedList> get() = _stationsListObs

    private val _currentStationObs = BehaviorRelay.create<Station>()
    val currentStationObs: Observable<Station> get() = _currentStationObs
    var currentStation: Station
        get() = _currentStationObs.value ?: Station()
        set(value) {
            _currentStationObs.accept(value)
            stationRepository.saveCurrentStationId(value.id)
        }

    init {
        stationsList.setOnChangeListener { _stationsListObs.accept(it) }

        Single.zip(stationRepository.getAllGroups(), stationRepository.getAllStations(),
                BiFunction { groups: List<Group>, stations: List<Station> ->
                    stationsList.init(groups, stations)
                })
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    fun getStation(id: Int): Station? {
        return stationsList.getGroupItem(id)
    }

    fun haveStations(): Boolean {
        return stationsList.size != 0
    }

    fun createStation(uri: Uri): Single<Station> {
        return stationRepository.createStation(uri)
                .doOnSuccess {
                    previousWhenCreate = currentStation
                    currentStation = it
                }
    }

    fun addStation(station: Station): Completable {
        val group = if (station.group.isBlank()) Group.default() else Group(station.group)
        station.groupId = group.id

        return validate(station, adding = true)
                .doOnComplete {
                    stationsList.add(group)
                    stationsList.add(station)
                }
                .andThen(stationRepository.addGroup(group))
                .andThen(stationRepository.addStation(station))
                .doOnComplete { currentStation = station }
    }

    private fun validate(station: Station, adding: Boolean = false): Completable {
        return when {
            stationsList.contains { it.name == station.name } && adding -> {
                Completable.error(ValidationException(R.string.toast_name_exists_error))
            }
            station.name.isBlank() -> {
                Completable.error(ValidationException(R.string.toast_name_empty_error))
            }
            else -> Completable.complete()
        }
    }

    fun updateCurrentStation(newStation: Station): Completable {
        val currentPosition = stationsList.positionOfFirst(currentStation.id)

        val updateStation = if (newStation != currentStation) {
            stationRepository.updateStation(newStation)
        } else Completable.complete()

        val remove = if (newStation.name != currentStation.name) {
            stationRepository.removeStation(currentStation)
        } else Completable.complete()

        return validate(newStation)
                .doOnComplete {
                    currentStation = if (stationsList.contains { it.id == newStation.id }) {
                        newStation
                    } else {
                        val newPos = (stationsList.itemsSize + currentPosition - 1) % stationsList.itemsSize
                        stationsList.getGroupItem(newPos)
                    }
                }
    }

    fun nextStation() {
        val next = stationsList.getNextFrom(currentStation.id)
        if (next != null) currentStation = next
    }

    fun previousStation() {
        val previous = stationsList.getPreviousFrom(currentStation.id)
        if (previous != null) currentStation = previous
    }

    fun removeCurrentStation(): Completable {
        if (stationsList.isFirstStation(currentStation.id)) {
            previousStation()
        } else {
            nextStation()
        }
        val removed = stationsList.removeStation(currentStation.id)
        return stationRepository.removeStation(removed)
    }

    fun showOrHideGroup(id: String): Completable {
        val group = if (stationsList.isGroupExpanded(id)) {
            stationsList.collapseGroup(id)
        } else {
            stationsList.expandGroup(id)
        }
        return stationRepository.updateGroup(group)
    }

    fun addCurrentShortcut(): Boolean {
//        return shortcutHelper.pinShortcut(currentStation, currentIcon)
        return true
    }

//    //region =============== Icon ==============
//
//    fun iconChanged(): Single<Boolean> =
//            iconRepository.getSavedIcon(currentIcon.name)
//                    .map { currentIcon != it }
//
//    var currentIcon: Icon
//        get() = iconRepository.currentIcon.value
//        set(value) = iconRepository.currentIcon.accept(value)
//
//    val currentIconObs: Observable<Icon> get() = iconRepository.currentIcon
//
//    fun getIcon(path: String): Single<Icon> = iconRepository.getStationIcon(path)
//
//    private fun removeIcon(name: String): Completable {
//        return iconRepository.removeStationIcon(name)
//    }
//
//    private fun saveCurrentIcon(newName: String): Completable {
//        return iconChanged().flatMapCompletable { changed ->
//            if (changed || currentIcon.name != newName) {
//                iconRepository.saveStationIcon(newName)
//            } else {
//                Completable.complete()
//            }
//        }
//    }
//
//    //endregion
}
