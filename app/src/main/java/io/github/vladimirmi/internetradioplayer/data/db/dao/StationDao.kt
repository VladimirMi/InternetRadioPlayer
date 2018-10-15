package io.github.vladimirmi.internetradioplayer.data.db.dao

import android.arch.persistence.room.*
import io.github.vladimirmi.internetradioplayer.data.db.entity.Genre
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.db.entity.StationGenreJoin
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Dao
interface StationDao {

    @Query("SELECT * FROM station ORDER BY `order` ASC")
    fun getAllStations(): Single<List<Station>>

    @Query("SELECT * FROM `group` ORDER BY `order` ASC")
    fun getAllGroups(): Single<List<Group>>

    @Query("SELECT * FROM `group` WHERE name = :groupName")
    fun getGroupByName(groupName: String): Group

    @Query("SELECT * FROM genre")
    fun getAllGenres(): Single<List<Genre>>

    @Query("SELECT * FROM station_genre_join")
    fun getAllStationGenreJoins(): Single<List<StationGenreJoin>>

    @Query("SELECT * FROM genre INNER JOIN station_genre_join ON genre.name = genreName WHERE stationId = :id")
    fun getStationGenres(id: String): Single<List<Genre>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStation(station: Station): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertGenres(genres: List<Genre>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertStationGenre(stationGenreJoins: List<StationGenreJoin>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertGroup(group: Group): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateStation(station: Station)

    @Update
    fun updateGroup(group: Group)

    @Query("DELETE FROM station WHERE id = :id")
    fun deleteStation(id: String)

    @Delete
    fun delete(genre: Genre)

    @Delete
    fun delete(stationGenreJoin: StationGenreJoin)

    @Delete
    fun delete(group: Group)
}
