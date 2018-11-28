package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.FavoriteListRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 25.11.2018.
 */

class FavoriteListInteractor
@Inject constructor(private val favoriteListRepository: FavoriteListRepository) {

    fun initFavoriteList(): Completable {
        return Singles.zip(favoriteListRepository.getAllGroups(), favoriteListRepository.getAllStations())
        { groups, stations ->
            val map = stations.groupBy { it.groupId }
            groups.forEach { group -> group.stations = map[group.id]!! }
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
                favoriteListRepository.updateGroups(groupUpdates)
                        .andThen(favoriteListRepository.updateStations(stationUpdates))
                        .andThen(initFavoriteList())
            } else {
                favoriteListRepository.initStationsList(groups)
            }
        }
    }

    fun isFavorite(station: Station): Boolean {
        return favoriteListRepository.findStation { it.id == station.id } != null
    }

    fun createGroup(groupName: String): Completable {
        return Single.just(favoriteListRepository.groups)
                .map { groups -> groups.find { groupName == it.name } }
                .ignoreElement()
                .onErrorResumeNext {
                    val group = if (groupName == Group.DEFAULT_NAME) Group.default()
                    else Group(groupName, favoriteListRepository.groups.size)

                    favoriteListRepository.addGroup(group)
                }.subscribeOn(Schedulers.io())
    }


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