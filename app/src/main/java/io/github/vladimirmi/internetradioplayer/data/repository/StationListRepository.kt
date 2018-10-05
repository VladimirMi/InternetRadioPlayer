package io.github.vladimirmi.internetradioplayer.data.repository

import android.net.Uri
import io.github.vladimirmi.internetradioplayer.data.db.StationsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.Genre
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.db.entity.StationGenreJoin
import io.github.vladimirmi.internetradioplayer.data.manager.Preferences
import io.github.vladimirmi.internetradioplayer.data.manager.StationParser
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


    fun saveCurrentStationId(id: String) {
        preferences.currentStationId = id
    }

    fun getCurrentStationId() = preferences.currentStationId

    fun getAllStations(): Single<List<Station>> {
        return db.stationDao().getAllStations()
                .toObservable()
                .flatMapIterable { it }
                .flatMapSingle { station ->
                    db.stationDao().getStationGenres(station.id)
                            .map { station.apply { genres = it.map(Genre::name) } }
                }.toList()
    }

    fun getAllGroups(): Single<List<Group>> = db.stationDao().getAllGroups()

    fun getStationGenres(id: String): Single<List<Genre>> = db.stationDao().getStationGenres(id)

    fun createStation(uri: Uri): Single<Station> {
        return Single.fromCallable {
            stationParser.parseFromUri(uri)
        }
    }

    fun addGroup(group: Group): Completable {
        return Completable.fromCallable {
            db.stationDao().insertGroup(group)
        }
    }

    fun removeGroup(group: Group): Completable {
        TODO("not implemented")
    }

    fun updateGroups(groups: List<Group>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                groups.forEach { db.stationDao().updateGroup(it) }
            }
        }
    }

    fun addStation(station: Station): Completable {
        return Completable.fromCallable {
            val group = db.stationDao().getGroupByName(station.groupName)
            val newStation = if (station.groupId != group.id) {
                station.copy(groupId = group.id)
            } else station

            db.stationDao().insertStation(newStation)
            val genres = station.genres.map(::Genre)
            db.stationDao().insertGenres(genres)
            db.stationDao().insertStationGenre(genres.map { StationGenreJoin(station.id, it.name) })
        }
    }

    fun removeStation(station: Station): Completable {
        return Completable.fromCallable {
            db.stationDao().deleteStation(station.id)
        }
    }

    fun updateStations(stations: List<Station>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                stations.forEach { db.stationDao().updateStation(it) }
            }
        }
    }
}
