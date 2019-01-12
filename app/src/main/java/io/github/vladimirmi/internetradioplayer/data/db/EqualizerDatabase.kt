package io.github.vladimirmi.internetradioplayer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.vladimirmi.internetradioplayer.data.db.dao.EqualizerDao
import io.github.vladimirmi.internetradioplayer.data.db.entity.EqualizerPreset

/**
 * Created by Vladimir Mikhalev 12.01.2019.
 */

@Database(entities = [EqualizerPreset::class],
        version = 2, exportSchema = false)
abstract class EqualizerDatabase : RoomDatabase() {

    abstract fun equalizerDao(): EqualizerDao

    companion object {
        fun newInstance(context: Context): EqualizerDatabase {
            return Room.databaseBuilder(context.applicationContext,
                    EqualizerDatabase::class.java, "equalizer.db")
                    .fallbackToDestructiveMigrationFrom(1)
                    .build()
        }
    }
}