package io.github.vladimirmi.internetradioplayer.domain.interactor

import android.net.Uri
import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Genre
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.manager.ShortcutHelper
import io.github.vladimirmi.internetradioplayer.data.repository.StationListRepository
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.ValidationException
import io.github.vladimirmi.internetradioplayer.presentation.station.StationInfo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
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
    private var stationsList = FlatStationsList()

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
            groups.forEach { group ->
                val groupStations = map[group.id]
                groupStations?.let { group.stations = groupStations.toMutableList() }
            }
            groups
        }.flatMapCompletable { groups ->
            //todo optimize
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
        //todo optimize
        stationsList = FlatStationsList.createFrom(groups)
        _stationsListObs.accept(stationsList)
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

    fun getCurrentGroup(): Group {
        return groups.find { it.id == currentStation.groupId }!!
    }

    fun getCurrentGenres(): Single<List<Genre>> {
        return stationRepository.getStationGenres(currentStation.id)
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

    fun addCurrentStation(stationName: String, groupName: String): Completable {
        validate(stationName, adding = true)?.let { return it }

        return addGroup(groupName).flatMapCompletable { group ->
            val newStation = currentStation.copy(groupId = group.id, order = group.stations.size)
            stationRepository.addStation(newStation).doOnComplete {
                group.stations.add(newStation)
                currentStation = newStation
                buildStationsList()
            }
        }
    }

    fun updateCurrentStation(stationName: String, groupName: String): Completable {
        validate(stationName)?.let { return it }

        return addGroup(groupName).flatMapCompletable { group ->
            val order = if (currentStation.groupId != group.id) group.stations.size else currentStation.order
            val newStation = currentStation.copy(name = stationName, groupId = group.id, order = order)
            stationRepository.updateStations(listOf(newStation))
                    .doOnComplete { currentStation = newStation }
            //todo optimize
        }.andThen(buildGroupsList())
    }

    fun removeStation(id: String): Completable {
        val station = getStation(id)
                ?: return Completable.error(IllegalStateException("Can not find station with id $id"))

        return stationRepository.removeStation(station)
                .doOnComplete { if (stationsList.isFirstStation(id)) nextStation(id) else previousStation(id) }
                //todo optimize
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

    fun stationChanged(stationInfo: StationInfo): Boolean {
        if (currentStation.icon != previousWhenEdit!!.icon) return false
        if (stationInfo.stationName != previousWhenEdit!!.name) return false
        if (stationInfo.groupName != getCurrentGroup().name) return false
        return true
    }

    fun moveGroupElements(stations: FlatStationsList): Completable {
        val updateGroups = Single.fromCallable { FlatStationsList.createFrom(groups).getGroupUpdatesFrom(stations) }
                .flatMapCompletable { stationRepository.updateGroups(it) }

        val updateStations = Single.fromCallable { FlatStationsList.createFrom(groups).getStationUpdatesFrom(stations) }
                .flatMapCompletable { stationRepository.updateStations(it) }

        return updateGroups.andThen(updateStations).andThen(buildGroupsList())
    }

    private fun addGroup(name: String): Single<Group> {
        groups.find { it.name == name }?.let { return Single.just(it) }

        val group = Group(if (name == Group.DEFAULT_NAME) Group.DEFAULT_ID else UUID.randomUUID().toString(),
                name, groups.size)

        return stationRepository.addGroup(group)
                .andThen(Single.fromCallable {
                    groups.add(group)
                    buildStationsList()
                    group
                })
    }

    private fun containsStation(predicate: (Station) -> Boolean): Boolean {
        return groups.any { it.stations.any(predicate) }
    }

    private fun validate(stationName: String, adding: Boolean = false): Completable? {
        return when {
            adding && containsStation { it.name == stationName } -> {
                Completable.error(ValidationException(R.string.toast_name_exists_error))
            }
            stationName.isBlank() -> {
                Completable.error(ValidationException(R.string.toast_name_empty_error))
            }
            else -> null
        }
    }

    private fun indexOfGroup(groupId: String): Int {
        return groups.indexOfFirst { it.id == groupId }
    }
}

