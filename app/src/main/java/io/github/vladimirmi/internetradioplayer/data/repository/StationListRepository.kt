package io.github.vladimirmi.internetradioplayer.data.repository

import android.net.Uri
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.utils.Preferences
import io.github.vladimirmi.internetradioplayer.data.utils.StationParser
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationListRepository
@Inject constructor(private val stationParser: StationParser,
                    private val preferences: Preferences,
                    private val db: StationsDatabase) {

    private val dao = db.stationDao()

    fun saveCurrentStationId(id: String) {
        preferences.currentStationId = id
    }

    fun getCurrentStationId() = preferences.currentStationId

    fun getAllStations(): Single<List<Station>> {
        return dao.getAllStations()
                .toObservable()
                .flatMapIterable { it }
                .toList()
    }

    fun getAllGroups(): Single<List<Group>> = dao.getAllGroups()

    fun createStation(uri: Uri): Single<Station> {
        return Single.fromCallable {
            stationParser.parseFromUri(uri)
        }
    }

    fun addGroup(group: Group): Completable {
        return Completable.fromCallable {
            dao.insertGroup(group)
        }
    }

    fun removeGroup(group: Group): Completable {
        TODO("not implemented")
    }

    fun updateGroups(groups: List<Group>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                groups.forEach { dao.updateGroup(it) }
            }
        }
    }

    fun addStation(station: Station): Completable {
//        return Completable.fromCallable {
//            val group = dao.getGroupByName(station.groupName)
//            val newStation = if (station.groupId != group.id) {
//                station.copy(groupId = group.id)
//            } else station
//
//            dao.insertStation(newStation)
//            val genres = station.genres.map(::Genre)
//            dao.insertGenres(genres)
//            dao.insertStationGenre(genres.map { StationGenreJoin(station.id, it.name) })
//        }
        return Completable.complete()
    }

    fun removeStation(station: Station): Completable {
        return Completable.fromCallable {
            dao.deleteStation(station.id)
        }
    }

    fun updateStations(stations: List<Station>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                stations.forEach { dao.updateStation(it) }
            }
        }
    }
}
