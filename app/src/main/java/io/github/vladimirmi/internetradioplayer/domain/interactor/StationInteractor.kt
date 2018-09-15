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
        return buildGroupsList().doOnComplete {
            val savedCurrentStation = getStation(stationRepository.getCurrentStationId())
            savedCurrentStation?.let { currentStation = it }
        }
    }

    private fun buildGroupsList(): Completable {
        return Singles.zip(stationRepository.getAllGroups(), stationRepository.getAllStations())
        { groups, stations ->
            val map = stations.groupBy { it.groupId }
            groups.map { group -> group.apply { map[id]?.let { group.stations = it.toMutableList() } } }

        }.flatMapCompletable { groups ->
            val groupUpdates = arrayListOf<Group>()
            val stationUpdates = arrayListOf<Station>()

            groups.forEachIndexed { i, group ->
                if (group.order != i) groupUpdates.add(group.copy(order = i))
                group.stations.forEachIndexed { j, station ->
                    if (station.order != j) stationUpdates.add(station.copy(order = j))
                }
            }
            if (groupUpdates.isNotEmpty() || stationUpdates.isNotEmpty()) {
                stationRepository.updateGroups(groupUpdates)
                        .andThen(stationRepository.updateStations(stationUpdates))
                        .andThen(buildGroupsList())
            } else {
                Completable.fromCallable {
                    this.groups.clear()
                    this.groups.addAll(groups)
                    buildStationsList()
                }
            }
        }
    }

    private fun buildStationsList() {
        _stationsListObs.accept(FlatStationsList(groups))
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

        return addGroup(station.group).flatMapCompletable { group ->
            val order = if (station.groupId != group.id) group.stations.size else station.order
            val newStation = station.copy(groupId = group.id, order = order)
            stationRepository.updateStations(listOf(newStation))
                    .doOnComplete { currentStation = newStation }
        }.andThen(buildGroupsList())
    }

    fun removeStation(id: String): Completable {
        val station = getStation(id)
                ?: return Completable.error(IllegalStateException("Can not find station with id $id"))

        return stationRepository.remove(station)
                .doOnComplete { if (stationsList.isFirstStation(id)) nextStation(id) else previousStation(id) }
                .andThen(buildGroupsList())
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
        newGroup.stations = group.stations
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

