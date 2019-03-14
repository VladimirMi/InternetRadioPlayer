package io.github.vladimirmi.internetradioplayer.data.db.dao

import androidx.room.*
import io.github.vladimirmi.internetradioplayer.data.db.entity.SuggestionEntity
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

@Dao
interface SuggestionsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(suggestion: SuggestionEntity)

    @Query("SELECT * FROM suggestionentity WHERE value LIKE :query ORDER BY lastModified DESC")
    fun getSuggestions(query: String): Single<List<SuggestionEntity>>

    @Query("SELECT * FROM suggestionentity WHERE value = :value")
    fun getSuggestion(value: String): Maybe<SuggestionEntity>

    @Delete
    fun delete(suggestion: SuggestionEntity)
}
