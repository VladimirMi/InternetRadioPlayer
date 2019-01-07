package io.github.vladimirmi.internetradioplayer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.vladimirmi.internetradioplayer.data.db.dao.SuggestionsDao
import io.github.vladimirmi.internetradioplayer.data.db.entity.SuggestionEntity

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

@Database(entities = [SuggestionEntity::class],
        version = 2, exportSchema = false)
abstract class SuggestionsDatabase : RoomDatabase() {

    abstract fun suggestionsDao(): SuggestionsDao

    companion object {
        fun newInstance(context: Context): SuggestionsDatabase {
            return Room.databaseBuilder(context.applicationContext,
                    SuggestionsDatabase::class.java, "suggestions.db")
                    .fallbackToDestructiveMigrationFrom(1)
                    .build()
        }
    }
}
