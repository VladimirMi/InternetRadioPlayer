package io.github.vladimirmi.internetradioplayer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.vladimirmi.internetradioplayer.data.db.dao.StationDao
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station

/**
 * Created by Vladimir Mikhalev 28.08.2018.
 */

@Database(entities = [Station::class, Group::class],
        version = 2, exportSchema = true)
abstract class StationsDatabase : RoomDatabase() {

    //todo rename dao to favorites
    abstract fun stationDao(): StationDao

    companion object {
        fun newInstance(context: Context): StationsDatabase {
            return Room.databaseBuilder(context.applicationContext,
                    StationsDatabase::class.java, "stations.db")
                    .build()
        }
    }
}
