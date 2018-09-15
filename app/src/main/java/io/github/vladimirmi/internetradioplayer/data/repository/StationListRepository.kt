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
import timber.log.Timber
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

    fun getAllStations(): Single<List<Station>> = db.stationDao().getAllStations()

    fun getAllGroups(): Single<List<Group>> = db.stationDao().getAllGroups()

    fun getAllGenres(): Single<List<Genre>> = db.stationDao().getAllGenres()

    fun getAllStationGenreJoins(): Single<List<StationGenreJoin>> = db.stationDao().getAllStationGenreJoins()

    fun createStation(uri: Uri): Single<Station> {
        return Single.fromCallable {
            stationParser.parseFromUri(uri)
        }
    }

    fun add(group: Group): Completable {
        return Completable.fromCallable {
            db.stationDao().insertGroup(group)
        }
    }

    fun remove(group: Group): Completable {
        TODO("not implemented")
    }

    fun updateGroups(groups: List<Group>): Completable {
        return Completable.fromCallable {
            db.runInTransaction {
                groups.forEach { db.stationDao().update(it) }
            }
        }
    }

    fun add(station: Station): Completable {
        Timber.e("add: ${station.name}")
        return Completable.fromCallable {
            db.stationDao().insertStation(station)
            val genres = station.genres.map(::Genre)
            db.stationDao().insertGenres(genres)
            db.stationDao().insertStationGenre(genres.map { StationGenreJoin(station.id, it.name) })
        }
    }

    fun remove(station: Station): Completable {
        return Completable.fromCallable {
            db.stationDao().deleteStation(station.id)
        }
    }

    fun updateStations(stations: List<Station>): Completable {
        Timber.e("updateStations: ${stations.size}")
        return Completable.fromCallable {
            db.runInTransaction {
                stations.forEach { db.stationDao().update(it) }
            }
        }
    }
}
