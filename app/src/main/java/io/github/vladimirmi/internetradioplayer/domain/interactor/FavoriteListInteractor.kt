package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.GroupListRepository
import io.github.vladimirmi.internetradioplayer.data.repository.StationRepository
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 25.11.2018.
 */

class FavoriteListInteractor
@Inject constructor(private val groupListRepository: GroupListRepository,
                    private val stationRepository: StationRepository) {

    val stationsListObs: Observable<FlatStationsList>
        get() = groupListRepository.stationsListObs

    fun initFavoriteList(): Completable {
        return Singles.zip(groupListRepository.getAllGroups(), stationRepository.getFavoriteStations())
        { groups, stations ->
            val map = stations.groupBy { it.groupId }
            groups.forEach { group -> group.stations = map[group.id] ?: emptyList() }
            groups
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
                groupListRepository.updateGroups(groupUpdates)
                        .andThen(stationRepository.updateStations(stationUpdates))
                        .andThen(initFavoriteList())
            } else {
                groupListRepository.initStationsList(groups)
            }
        }
    }

    fun isFavorite(station: Station) = isFavorite(station.id)

    fun createGroup(groupName: String): Completable {
        return Single.fromCallable { groupListRepository.groups }
                .map { groups -> groups.find { groupName == it.name } ?: Group.nullObj() }
                .flatMapCompletable {
                    if (!it.isNull()) Completable.complete()
                    else {
                        val group = if (groupName == Group.DEFAULT_NAME) Group.default()
                        else Group(groupName)
                        groupListRepository.addGroup(group)
                    }
                }.andThen(initFavoriteList())
    }

    fun getGroup(id: String): Single<Group> {
        return Single.fromCallable {
            groupListRepository.groups.find { it.id == id } ?: Group.nullObj()
        }
                .flatMap {
                    if (it.isNull()) createGroup(Group.DEFAULT_NAME)
                            .andThen(getGroup(Group.DEFAULT_ID))
                    else Single.just(it)
                }
    }

    fun getGroupsObs(): Observable<List<Group>> {
        return groupListRepository.stationsListObs
                .map { groupListRepository.groups }
    }

    fun expandOrCollapseGroup(id: String): Completable {
        return getGroup(id)
                .map { it.copy(expanded = !it.expanded) }
                .flatMapCompletable {
                    groupListRepository.updateGroups(listOf(it))
                            .andThen(initFavoriteList())
                }
    }

    fun moveGroupElements(stations: FlatStationsList): Completable {
        val updateGroups = Single.fromCallable { groupListRepository.stations.getGroupDifference(stations) }
                .flatMapCompletable { groupListRepository.updateGroups(it) }

        val updateStations = Single.fromCallable { groupListRepository.stations.getStationDifference(stations) }
                .flatMapCompletable { stationRepository.updateStations(it) }

        return updateGroups.andThen(updateStations).andThen(initFavoriteList())
    }

    fun nextStation(id: String): Boolean {
        if (!isFavorite(id)) return false
        val next = groupListRepository.stations.getNextStationFrom(id)
        next?.let { stationRepository.station = it }
        return next != null
    }

    fun previousStation(id: String): Boolean {
        if (!isFavorite(id)) return false
        val previous = groupListRepository.stations.getPreviousStationFrom(id)
        previous?.let { stationRepository.station = it }
        return previous != null
    }

    private fun isFavorite(id: String) = groupListRepository.stations.findStation { it.id == id } != null
}