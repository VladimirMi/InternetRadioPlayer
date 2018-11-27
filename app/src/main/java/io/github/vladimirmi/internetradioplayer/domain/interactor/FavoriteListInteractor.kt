package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 25.11.2018.
 */

class FavoriteListInteractor
@Inject constructor() {

    fun initFavoriteList(): Completable {
        return Completable.complete()
//        return buildGroupsList().doOnComplete {
//            val savedCurrentStation = getStation(stationRepository.getCurrentStationId())
//            station = savedCurrentStation ?: stationsList.getFirstStation() ?: Station.nullObj()
//        }
    }

//    private fun buildGroupsList(): Completable {
//        return Singles.zip(stationRepository.getAllGroups(), stationRepository.getAllStations())
//        { groups, stations ->
//            val map = stations.groupBy { it.groupId }
//            groups.forEach { group ->
//                val groupStations = map[group.id]
//                groupStations?.let { group.stations = groupStations.toMutableList() }
//            }
//            groups
//        }.flatMapCompletable { groups ->
//            //todo optimize
//            val groupUpdates = arrayListOf<Group>()
//            val stationUpdates = arrayListOf<Station>()
//
//            groups.forEachIndexed { i, group ->
//                if (group.order != i) groupUpdates.add(group.copy(order = i))
//                group.stations.forEachIndexed { j, station ->
//                    if (station.order != j) stationUpdates.add(station.copy(order = j))
//                }
//            }
//            if (groupUpdates.isNotEmpty() || stationUpdates.isNotEmpty()) {
//                stationRepository.updateGroups(groupUpdates)
//                        .andThen(stationRepository.updateStations(stationUpdates))
//                        .andThen(buildGroupsList())
//            } else {
//                Completable.fromCallable {
//                    this.groups.clear()
//                    this.groups.addAll(groups)
//                    buildStationsList()
//                }
//            }
//        }
//    }

//    private fun buildStationsList() {
//        //todo optimize
//        stationsList = FlatStationsList.createFrom(groups)
//        _stationsListObs.accept(stationsList)
//    }


//    fun addCurrentStation(stationName: String, groupName: String): Completable {
//        validate(stationName, adding = true)?.let { return it }
//
//        return addGroup(groupName).flatMapCompletable { group ->
//            val newStation = station.copy(groupId = group.id, order = group.stations.size)
//            stationRepository.addStation(newStation).doOnComplete {
//                group.stations.add(newStation)
//                station = newStation
//                buildStationsList()
//            }
//        }
//    }

//    fun updateCurrentStation(stationName: String, groupName: String): Completable {
//        validate(stationName)?.let { return it }
//
//        return addGroup(groupName).flatMapCompletable { group ->
//            val order = if (station.groupId != group.id) group.stations.size else station.order
//            val newStation = station.copy(name = stationName, groupId = group.id, order = order)
//            stationRepository.updateStations(listOf(newStation))
//                    .doOnComplete { station = newStation }
//            //todo optimize
//        }.andThen(buildGroupsList())
//    }

//    fun removeStation(id: String): Completable {
//        val station = getStation(id)
//                ?: return Completable.error(IllegalStateException("Can not find station with id $id"))
//
//        return stationRepository.removeStation(station)
//                .doOnComplete {
//                    val changed = if (stationsList.isFirstStation(id)) nextStation(id)
//                    else previousStation(id)
//                    if (!changed) station = Station.nullObj()
//                }
//                //todo optimize
//                .andThen(buildGroupsList())
//    }

//    fun nextStation(id: String = station.id): Boolean {
//        val next = stationsList.getNextFrom(id)
//        next?.let { station = it }
//        return next != null
//    }

//    fun previousStation(id: String = station.id): Boolean {
//        val previous = stationsList.getPreviousFrom(id)
//        previous?.let { station = it }
//        return previous != null
//    }

//    fun expandOrCollapseGroup(id: String): Completable {
//        val i = indexOfGroup(id)
//        val group = groups[i]
//        val newGroup = group.copy(expanded = !group.expanded)
//        newGroup.stations = group.stations
//        return stationRepository.updateGroups(listOf(newGroup))
//                .doOnComplete {
//                    groups[i] = newGroup
//                    buildStationsList()
//                }
//    }

//    fun moveGroupElements(stations: FlatStationsList): Completable {
//        val updateGroups = Single.fromCallable { FlatStationsList.createFrom(groups).getGroupUpdatesFrom(stations) }
//                .flatMapCompletable { stationRepository.updateGroups(it) }
//
//        val updateStations = Single.fromCallable { FlatStationsList.createFrom(groups).getStationUpdatesFrom(stations) }
//                .flatMapCompletable { stationRepository.updateStations(it) }
//
//        return updateGroups.andThen(updateStations).andThen(buildGroupsList())
//                .doOnComplete { station = getStation(station.id) ?: Station.nullObj() }
//    }

//    private fun addGroup(name: String): Single<Group> {
//        groups.find { it.name == name }?.let { return Single.just(it) }
//
//        val group = if (name == Group.DEFAULT_NAME) Group.default() else Group(name, groups.size)
//
//        return stationRepository.addGroup(group)
//                .andThen(Single.fromCallable {
//                    groups.add(group)
//                    buildStationsList()
//                    group
//                })
//    }

//    private fun containsStation(predicate: (Station) -> Boolean): Boolean {
//        return groups.any { it.stations.any(predicate) }
//    }

//    private fun validate(stationName: String, adding: Boolean = false): Completable? {
//        return when {
//            adding && containsStation { it.name == stationName } -> {
//                Completable.error(MessageResException(R.string.msg_name_exists_error))
//            }
//            stationName.isBlank() -> {
//                Completable.error(MessageResException(R.string.msg_name_empty_error))
//            }
//            else -> null
//        }
//    }

//    private fun indexOfGroup(groupId: String): Int {
//        return groups.indexOfFirst { it.id == groupId }
//    }


}