package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.MediaRepository
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
@Inject constructor(private val favoritesRepository: FavoritesRepository,
                    private val stationRepository: StationRepository,
                    private val mediaRepository: MediaRepository) {

    val stationsListObs: Observable<FlatStationsList>
        get() = favoritesRepository.stationsListObs

    fun initFavoriteList(): Completable {
        return Singles.zip(favoritesRepository.getAllGroups(), favoritesRepository.getAllStations())
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
                favoritesRepository.updateGroups(groupUpdates)
                        .andThen(favoritesRepository.updateStations(stationUpdates))
                        .andThen(initFavoriteList())
            } else {
                favoritesRepository.initStationsList(groups)
            }
        }
    }

    fun isFavorite(station: Station) = isFavorite(station.id)

    fun createGroup(groupName: String): Completable {
        return Single.fromCallable { favoritesRepository.groups }
                .map { groups -> groups.find { groupName == it.name } ?: Group.nullObj() }
                .flatMapCompletable {
                    if (!it.isNull()) Completable.complete()
                    else {
                        val group = if (groupName == Group.DEFAULT_NAME) Group.default()
                        else Group(groupName)
                        favoritesRepository.addGroup(group)
                    }
                }.andThen(initFavoriteList())
    }

    fun getGroup(id: String): Single<Group> {
        return Single.fromCallable {
            favoritesRepository.groups.find { it.id == id } ?: Group.nullObj()
        }
                .flatMap {
                    if (it.isNull()) createGroup(Group.DEFAULT_NAME)
                            .andThen(getGroup(Group.DEFAULT_ID))
                    else Single.just(it)
                }
    }

    fun getGroupsObs(): Observable<List<Group>> {
        return favoritesRepository.stationsListObs
                .map { favoritesRepository.groups }
    }

    fun expandOrCollapseGroup(id: String): Completable {
        return getGroup(id)
                .map { it.copy(expanded = !it.expanded) }
                .flatMapCompletable { favoritesRepository.updateGroups(listOf(it)) }
                .andThen(initFavoriteList())
    }

    fun removeGroup(id: String): Completable {
        return getGroup(id)
                .flatMapCompletable { favoritesRepository.removeGroup(it) }
                .andThen(initFavoriteList())
    }

    fun moveGroupElements(stations: FlatStationsList): Completable {
        val updateGroups = Single.fromCallable { favoritesRepository.stations.getGroupDifference(stations) }
                .flatMapCompletable { favoritesRepository.updateGroups(it) }

        val updateStations = Single.fromCallable { favoritesRepository.stations.getStationDifference(stations) }
                .doOnSuccess { list -> updateCurrentStation(list) }
                .flatMapCompletable { favoritesRepository.updateStations(it) }

        return updateGroups.andThen(updateStations).andThen(initFavoriteList())
    }

    fun getStation(id: String): Station? {
        return favoritesRepository.getStation { it.id == id }
    }

    fun nextStation(id: String): Boolean {
        val next = favoritesRepository.stations.getNextStationFrom(id)
        next?.let { mediaRepository.currentMedia = it }
        return next != null
    }

    fun previousStation(id: String): Boolean {
        val previous = favoritesRepository.stations.getPreviousStationFrom(id)
        previous?.let { mediaRepository.currentMedia = it }
        return previous != null
    }

    private fun isFavorite(id: String) = favoritesRepository.getStation { it.id == id } != null

    private fun updateCurrentStation(list: List<Station>) {
        val station = mediaRepository.currentMedia as? Station ?: return
        list.find { it.id == station.id }?.let {
            mediaRepository.currentMedia = it
        }
    }
}
