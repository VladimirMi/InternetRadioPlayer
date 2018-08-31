package io.github.vladimirmi.internetradioplayer.model.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import io.github.vladimirmi.internetradioplayer.model.db.dao.StationDao
import io.github.vladimirmi.internetradioplayer.model.db.entity.Genre
import io.github.vladimirmi.internetradioplayer.model.db.entity.Group
import io.github.vladimirmi.internetradioplayer.model.db.entity.Station
import io.github.vladimirmi.internetradioplayer.model.db.entity.StationGenreJoin

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Database(entities = [Station::class, Genre::class, StationGenreJoin::class, Group::class],
        version = 1)
abstract class StationsDatabase : RoomDatabase() {

    abstract fun stationDao(): StationDao

    companion object {
        fun newInstance(context: Context): StationsDatabase {
            return Room.databaseBuilder(context.applicationContext,
                    StationsDatabase::class.java, "stations.db")
                    .build()
        }
    }
}
