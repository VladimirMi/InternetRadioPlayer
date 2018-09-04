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

    @Query("SELECT * FROM genre")
    fun getAllGenres(): Single<List<Genre>>

    @Query("SELECT * FROM station_genre_join")
    fun getAllStationGenreJoins(): Single<List<StationGenreJoin>>

    @Query("SELECT * FROM genre INNER JOIN station_genre_join ON genre.name = genreName WHERE stationId = :id")
    fun getStationGenres(id: Int): Single<List<Genre>>

    @Insert
    fun insertStation(station: Station): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertGenres(genres: List<Genre>)

    @Insert
    fun insertStationGenre(stationGenreJoins: List<StationGenreJoin>)

    @Insert
    fun insertGroup(group: Group): Long

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
