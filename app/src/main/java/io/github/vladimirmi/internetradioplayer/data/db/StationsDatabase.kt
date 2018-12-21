package io.github.vladimirmi.internetradioplayer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
                    .addMigrations(MIGRATION_1_2)
                    .build()
        }
    }
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `station_temp` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `uri` TEXT NOT NULL, " +
                "`url` TEXT, `encoding` TEXT, `bitrate` TEXT, `sample` TEXT, `order` INTEGER NOT NULL, " +
                "`group_id` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`group_id`) REFERENCES `Group`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )")

        database.execSQL("INSERT INTO station_temp SELECT id, name, uri, url, 'mp3', bitrate, sample, `order`, group_id FROM station")
        database.execSQL("DROP TABLE station_genre_join")
        database.execSQL("DROP TABLE station")
        database.execSQL("DROP TABLE genre")
        database.execSQL("ALTER TABLE station_temp RENAME TO station")
        database.execSQL("CREATE INDEX `index_Station_group_id` ON `station` (`group_id`)")
        database.execSQL("CREATE UNIQUE INDEX `index_Station_uri` ON `station` (`uri`)")
    }
}
