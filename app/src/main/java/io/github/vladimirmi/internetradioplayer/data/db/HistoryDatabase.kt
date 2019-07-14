package io.github.vladimirmi.internetradioplayer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.vladimirmi.internetradioplayer.data.db.dao.HistoryDao
import io.github.vladimirmi.internetradioplayer.data.db.entity.History

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

@Database(entities = [History::class],
        version = 5, exportSchema = true)
abstract class HistoryDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        fun newInstance(context: Context): HistoryDatabase {
            return Room.databaseBuilder(context.applicationContext,
                    HistoryDatabase::class.java, "history.db")
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE history ADD COLUMN equalizerPreset TEXT")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE history ADD COLUMN remoteId TEXT DEFAULT '' NOT NULL")
        database.execSQL("ALTER TABLE history ADD COLUMN description TEXT")
        database.execSQL("ALTER TABLE history ADD COLUMN genre TEXT")
        database.execSQL("ALTER TABLE history ADD COLUMN language TEXT")
        database.execSQL("ALTER TABLE history ADD COLUMN location TEXT")
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `history_temp` (`timestamp` INTEGER NOT NULL, `id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, `uri` TEXT NOT NULL, `encoding` TEXT, `bitrate` TEXT, `sample` TEXT, " +
                "`order` INTEGER NOT NULL, `group_id` TEXT NOT NULL, `equalizerPreset` TEXT, " +
                "`description` TEXT, `genre` TEXT, `language` TEXT, `location` TEXT, `url` TEXT, PRIMARY KEY(`uri`))")

        database.execSQL("INSERT INTO history_temp SELECT timestamp, id, name, uri, url, encoding, bitrate, sample, " +
                "`order`, group_id, equalizerPreset, description, genre, language, location FROM station")

        database.execSQL("DROP TABLE IF EXISTS history")
        database.execSQL("ALTER TABLE history_temp RENAME TO history")
    }
}