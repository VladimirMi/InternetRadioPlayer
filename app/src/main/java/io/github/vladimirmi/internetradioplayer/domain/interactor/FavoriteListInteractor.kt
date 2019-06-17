package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
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
@Inject constructor(private val favoritesRepository: FavoritesRepository,
                    private val mediaInteractor: MediaInteractor) {

    val stationsListObs: Observable<FlatStationsList>
        get() = favoritesRepository.stationsListObs

    fun initFavoriteList(): Completable {
        return Singles.zip(favoritesRepository.getAllGroups(), favoritesRepository.getAllStations())
        { groups, stations ->
            stations.forEach { it.isFavorite = true }
            val map = stations.groupBy { it.groupId }
            groups.forEach { group -> group.stations = map[group.id] ?: emptyList() }
            groups
        }.flatMapCompletable(this::adjustOrderThenInit)
    }

    fun createGroup(groupName: String): Completable {
        if (favoritesRepository.groups.find { groupName == it.name } != null) {
            return Completable.complete()
        }
        val group = if (groupName == Group.DEFAULT_NAME) Group.default()
        else Group(name = groupName, order = -1)
        return favoritesRepository.addGroup(group)
                .andThen(initFavoriteList())
    }

    fun findGroup(id: String): Group? {
        return favoritesRepository.groups.find { it.id == id }
    }

    fun getGroup(id: String): Single<Group> {
        return Single.fromCallable { findGroup(id) ?: Group.nullObj() }
                .flatMap {
                    if (it.isNull()) createGroup(Group.DEFAULT_NAME)
                            .andThen(getGroup(Group.DEFAULT_ID))
                    else Single.just(it)
                }
    }

    fun expandOrCollapseGroup(group: Group): Completable {
        return updateGroup(group.copy(expanded = !group.expanded))
    }

    fun updateGroup(group: Group): Completable {
        return favoritesRepository.updateGroups(listOf(group))
                .andThen(initFavoriteList())
    }

    fun deleteGroup(group: Group): Completable {
        return favoritesRepository.removeGroup(group)
                .andThen(initFavoriteList())
    }

    fun moveGroupElements(stations: FlatStationsList): Completable {
        val updateGroups = Single.fromCallable { favoritesRepository.stations.getGroupDifference(stations) }
                .flatMapCompletable { favoritesRepository.updateGroups(it) }

        val updateStations = Single.fromCallable { favoritesRepository.stations.getStationDifference(stations) }
                .flatMapCompletable { favoritesRepository.updateStations(it) }

        return updateGroups.andThen(updateStations).andThen(initFavoriteList())
    }

    fun getStation(id: String): Station? {
        return favoritesRepository.getStation { it.id == id }
    }

    private fun adjustOrderThenInit(groups: List<Group>): Completable {
        val groupUpdates = arrayListOf<Group>()
        val stationUpdates = arrayListOf<Station>()

        groups.forEachIndexed { i, group ->
            if (group.order != i) groupUpdates.add(group.copy(order = i))
            group.stations.forEachIndexed { j, station ->
                if (station.order != j) stationUpdates.add(station.copy(order = j))
            }
        }
        return if (groupUpdates.isNotEmpty() || stationUpdates.isNotEmpty()) {
            favoritesRepository.updateGroups(groupUpdates)
                    .andThen(favoritesRepository.updateStations(stationUpdates))
                    .andThen(initFavoriteList())
        } else {
            favoritesRepository.initStationsList(groups)
                    .doOnComplete {
                        getStation(mediaInteractor.getSavedMediaId())
                                ?.let { mediaInteractor.currentMedia = it }
                    }
        }
    }
}
