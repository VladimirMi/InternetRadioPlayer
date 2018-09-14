package io.github.vladimirmi.internetradioplayer.domain.interactor

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.manager.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.data.repository.StationListRepository
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.ValidationException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject


/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

class StationInteractor
@Inject constructor(private val stationRepository: StationListRepository,
                    private val shortcutHelper: ShortcutHelper) {

    private val groups = arrayListOf<Group>()

    var previousWhenEdit: Station? = null
        private set
    var createMode: Boolean = false
        private set

    private val _currentStationObs = BehaviorRelay.create<Station>()
    val currentStationObs: Observable<Station> get() = _currentStationObs

    var currentStation: Station
        get() = _currentStationObs.value ?: Station.nullObj()
        set(value) {
            _currentStationObs.accept(value)
            stationRepository.saveCurrentStationId(value.id)
        }
    private val _stationsListObs = BehaviorRelay.create<FlatStationsList>()
    val stationsListObs: Observable<FlatStationsList> get() = _stationsListObs
    private val stationsList = FlatStationsList()

    fun initStations(): Completable {

        val stationsSingle = Singles.zip(stationRepository.getAllStations(),
                stationRepository.getAllStationGenreJoins()) { stations, joins ->
            stations.forEach { station ->
                station.genres = joins.filter { it.stationId == station.id }
                        .map { it.genreName }
            }
            stations
        }.doOnSuccess { stations ->
            val savedCurrentStation = stations.find { it.id == stationRepository.getCurrentStationId() }
            savedCurrentStation?.let { currentStation = it }
        }

        return Singles.zip(stationRepository.getAllGroups(), stationsSingle) { groups, stations ->
            this.groups.addAll(groups)

            val groupBy = stations.groupBy { it.groupId }
            groups.forEach { group ->
                val items = groupBy[group.id] ?: return@forEach
                items.forEach { it.group = group.name }
                group.stations.addAll(items)
            }
            buildStationsList()
        }.toCompletable()
    }

    fun addCurrentShortcut(): Boolean {
        return shortcutHelper.pinShortcut(currentStation)
    }

    fun setEditMode(editMode: Boolean) {
        if (editMode) previousWhenEdit = currentStation
        else {
            previousWhenEdit = null
            createMode = false
        }
    }

    fun getStation(id: String): Station? {
        for (group in groups) {
            for (station in group.stations) {
                if (station.id == id) return station
            }
        }
        return null
    }

    fun haveStations(): Boolean {
        return groups.fold(0) { acc, group -> acc + group.stations.size } != 0
    }

    fun createStation(uri: Uri): Single<Station> {
        return stationRepository.createStation(uri)
                .doOnSuccess {
                    setEditMode(true)
                    createMode = true
                    currentStation = it
                }
    }

    fun addStation(station: Station): Completable {
        validate(station, adding = true)?.let { return it }

        return addGroup(station.group).flatMapCompletable { group ->
            val newStation = station.copy(groupId = group.id, order = group.stations.size)
            stationRepository.add(newStation).doOnComplete {
                group.stations.add(newStation)
                currentStation = newStation
                buildStationsList()
            }
        }
    }

    fun updateStation(station: Station): Completable {
        validate(station)?.let { return it }

        return stationRepository.updateStations(listOf(station))
                .doOnComplete {
                    val stations = groups[indexOfGroup(station.groupId)].stations
                    stations[stations.indexOfFirst { it.id == station.id }] = station
                    currentStation = station
                    buildStationsList()
                }
    }

    fun removeStation(id: String): Completable {
        val station = getStation(id)
                ?: return Completable.error(IllegalStateException("Can not find station with id $id"))

        return stationRepository.remove(station)
                .doOnComplete {
                    if (stationsList.isFirstStation(id)) nextStation(id) else previousStation(id)
                    groups[indexOfGroup(station.groupId)].stations.remove(station)
                    buildStationsList()
                }
    }

    fun nextStation(id: String = currentStation.id) {
        stationsList.getNextFrom(id)?.let { currentStation = it }
    }

    fun previousStation(id: String = currentStation.id) {
        stationsList.getPreviousFrom(id)?.let { currentStation = it }
    }

    fun expandOrCollapseGroup(id: String): Completable {
        val i = indexOfGroup(id)
        val group = groups[i]
        val newGroup = group.copy(expanded = !group.expanded)
        return stationRepository.updateGroups(listOf(newGroup))
                .doOnComplete {
                    groups[i] = newGroup
                    buildStationsList()
                }
    }

    private fun addGroup(name: String): Single<Group> {
        groups.find { it.name == name }?.let { return Single.just(it) }

        val group = Group(if (name == Group.DEFAULT_NAME) Group.DEFAULT_ID else UUID.randomUUID().toString(),
                name, groups.size)

        return stationRepository.add(group)
                .andThen(Single.fromCallable {
                    groups.add(group)
                    buildStationsList()
                    group
                })
    }

    private fun buildStationsList() {
        stationsList.build(groups)
        _stationsListObs.accept(stationsList)
    }

    private fun containsStation(predicate: (Station) -> Boolean): Boolean {
        return groups.any { it.stations.any(predicate) }
    }

    private fun validate(station: Station, adding: Boolean = false): Completable? {
        return when {
            adding && containsStation { it.name == station.name } -> {
                Completable.error(ValidationException(R.string.toast_name_exists_error))
            }
            station.name.isBlank() -> {
                Completable.error(ValidationException(R.string.toast_name_empty_error))
            }
            else -> null
        }
    }

    private fun indexOfGroup(groupId: String): Int {
        return groups.indexOfFirst { it.id == groupId }
    }
}

