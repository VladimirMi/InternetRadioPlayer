package io.github.vladimirmi.internetradioplayer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.vladimirmi.internetradioplayer.data.db.dao.HistoryDao
import io.github.vladimirmi.internetradioplayer.data.db.entity.History

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

@Database(entities = [History::class],
        version = 1, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        fun newInstance(context: Context): HistoryDatabase {
            return Room.databaseBuilder(context.applicationContext,
                    HistoryDatabase::class.java, "history.db")
                    .build()
        }
    }
}