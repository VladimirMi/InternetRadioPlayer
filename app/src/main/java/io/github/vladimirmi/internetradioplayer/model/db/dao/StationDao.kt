package io.github.vladimirmi.internetradioplayer.model.db.dao

import android.arch.persistence.room.*
import io.github.vladimirmi.internetradioplayer.model.db.entity.Genre
import io.github.vladimirmi.internetradioplayer.model.db.entity.Group
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station
import io.github.vladimirmi.internetradioplayer.model.db.entity.StationGenreJoin
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Dao
interface StationDao {

    @Query("SELECT * FROM station")
    fun getAllStations(): Single<List<Station>>

    @Query("SELECT * FROM `group`")
    fun getAllGroups(): Single<List<Group>>

    @Query("SELECT * FROM genre INNER JOIN station_genre_join ON genre.name = genreName WHERE stationId = :id")
    fun getStationGenres(id: Int): Single<List<Genre>>

    @Insert
    fun insert(station: Station)

    @Insert
    fun insert(genre: Genre)

    @Insert
    fun insert(stationGenreJoin: StationGenreJoin)

    @Insert
    fun insert(group: Group)

    @Update
    fun update(station: Station)

    @Update
    fun update(group: Group)

    @Delete
    fun delete(station: Station)

    @Delete
    fun delete(genre: Genre)

    @Delete
    fun delete(stationGenreJoin: StationGenreJoin)

    @Delete
    fun delete(group: Group)
}
