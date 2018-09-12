package io.github.vladimirmi.internetradioplayer.model.repository

import android.net.Uri
import io.github.vladimirmi.internetradioplayer.model.db.dao.StationDao
import io.github.vladimirmi.internetradioplayer.model.db.entity.Genre
import io.github.vladimirmi.internetradioplayer.model.db.entity.Group
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station
import io.github.vladimirmi.internetradioplayer.model.db.entity.StationGenreJoin
import io.github.vladimirmi.internetradioplayer.model.entity.groupedlist.StationsGroupListener
import io.github.vladimirmi.internetradioplayer.model.manager.Preferences
import io.github.vladimirmi.internetradioplayer.model.manager.StationParser
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationListRepository
@Inject constructor(private val stationParser: StationParser,
                    private val preferences: Preferences,
                    private val dao: StationDao) : StationsGroupListener {


    fun saveCurrentStationId(id: String) {
        preferences.currentStationId = id
    }

    fun getCurrentStationId() = preferences.currentStationId

    fun getAllStations(): Single<List<Station>> = dao.getAllStations()

    fun getAllGroups(): Single<List<Group>> = dao.getAllGroups()

    fun getAllGenres(): Single<List<Genre>> = dao.getAllGenres()

    fun getAllStationGenreJoins(): Single<List<StationGenreJoin>> = dao.getAllStationGenreJoins()

    fun createStation(uri: Uri): Single<Station> {
        return Single.fromCallable {
            stationParser.parseFromUri(uri)
        }
    }

    fun updateStation(station: Station): Completable {
        return Completable.fromCallable {
            dao.update(station)
        }
    }

    override fun onGroupAdd(group: Group): Completable {
        return Completable.fromCallable {
            dao.insertGroup(group)
        }
    }

    override fun onGroupRemove(group: Group): Completable {
        TODO("not implemented")
    }

    override fun onGroupUpdate(groups: List<Group>): Completable {
        return Completable.fromCallable {
            groups.forEach { dao.update(it) }
        }
    }

    override fun onStationAdd(station: Station): Completable {
        return Completable.fromCallable {
            dao.insertStation(station)
            val genres = station.genres.map(::Genre)
            dao.insertGenres(genres)
            dao.insertStationGenre(genres.map { StationGenreJoin(station.id, it.name) })
        }
    }

    override fun onStationRemove(station: Station): Completable {
        return Completable.fromCallable {
            dao.deleteStation(station.id)
        }
    }

    override fun onStationUpdate(stations: List<Station>): Completable {
        TODO("not implemented")
    }
}
