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
        version = 3, exportSchema = true)
abstract class HistoryDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        fun newInstance(context: Context): HistoryDatabase {
            return Room.databaseBuilder(context.applicationContext,
                    HistoryDatabase::class.java, "history.db")
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigrationFrom(1)
                    .build()
        }
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE history ADD COLUMN equalizerId TEXT")
    }
}