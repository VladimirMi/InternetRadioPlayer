package io.github.vladimirmi.internetradioplayer.data.db.dao

import androidx.room.*
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Dao
interface StationDao {

    @Query("SELECT * FROM station ORDER BY `order` ASC")
    fun getFavoriteStations(): Single<List<Station>>

    @Query("SELECT * FROM `group` ORDER BY `order` ASC")
    fun getAllGroups(): Single<List<Group>>

    @Query("SELECT * FROM station WHERE id = :id")
    fun getStation(id: String): Single<Station>

    @Query("SELECT * FROM `group` WHERE id = :id")
    fun getGroup(id: String): Single<Group>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStation(station: Station): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertGroup(group: Group): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateStation(station: Station)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateGroup(group: Group)

    @Query("DELETE FROM station WHERE id = :id")
    fun deleteStation(id: String)

    @Query("DELETE FROM `group` WHERE id = :id")
    fun deleteGroup(id: String)

}
