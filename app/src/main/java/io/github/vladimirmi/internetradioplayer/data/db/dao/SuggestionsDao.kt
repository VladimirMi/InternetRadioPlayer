package io.github.vladimirmi.internetradioplayer.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.vladimirmi.internetradioplayer.data.db.entity.SuggestionEntity
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

@Dao
interface SuggestionsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(suggestion: SuggestionEntity)

    @Query("SELECT * FROM suggestionentity WHERE value LIKE :query")
    fun getSuggestions(query: String): Single<List<SuggestionEntity>>
}
